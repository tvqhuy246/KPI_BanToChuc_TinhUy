-- =========================================================================
-- HỆ THỐNG CƠ SỞ DỮ LIỆU ĐÁNH GIÁ KPI - BAN TỔ CHỨC TỈNH ỦY ĐẮK LẮK
-- Căn cứ: Quy định số 01-QĐ/BTCTU ngày 28/11/2025
-- Bổ sung: Phân loại Văn bản (Dễ - Khó), Họp (Phát biểu duyệt), Đi công tác, Công sở
-- Tổng hợp: Ngày, Tuần, Tháng & Ban Tổ chức đánh giá Phòng
-- =========================================================================

-- 1. Phòng chuyên môn
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Vị trí việc làm
CREATE TABLE IF NOT EXISTS positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    job_description TEXT
);

-- 3. Cán bộ, công chức (Users)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20),
    department_id BIGINT,
    position_id BIGINT,
    role VARCHAR(50) NOT NULL, -- ADMIN, LANH_DAO_BAN, TRUONG_PHONG, CHUYEN_VIEN
    status VARCHAR(20) DEFAULT ''ACTIVE'',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_pos FOREIGN KEY (position_id) REFERENCES positions(id) ON DELETE SET NULL
);

-- 4. Bộ tiêu chí định lượng KPI
CREATE TABLE IF NOT EXISTS kpi_criteria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    group_type VARCHAR(10) NOT NULL, -- A: Phẩm chất đạo đức/công sở, B: Chuyên môn, C: Sáng kiến
    max_score DOUBLE NOT NULL,
    scoring_guide TEXT,
    target_role VARCHAR(50) DEFAULT ''ALL''
);

-- 5. Bảng Nhiệm vụ công tác (Tasks) - Có phân loại Văn bản, Họp, Đi công tác, Công sở
CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    assigned_to BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    criteria_id BIGINT,
    
    -- Phân loại nghiệp vụ theo yêu cầu thực tế
    task_type VARCHAR(50) DEFAULT ''VAN_BAN'', -- VAN_BAN, HOP, CONG_TAC, CONG_SO
    complexity VARCHAR(30) DEFAULT ''TRUNG_BINH'', -- DE (x1.0), TRUNG_BINH (x1.2), KHO (x1.5), DAC_BIET (x2.0)
    
    -- Nghiệp vụ đặc thù
    speech_approved BOOLEAN DEFAULT FALSE, -- Cuộc họp: Xây dựng bài phát biểu / ý kiến được duyệt
    field_trip_location VARCHAR(255),      -- Đi công tác: Địa bàn thẩm tra / cơ sở
    field_trip_result TEXT,               -- Báo cáo kết quả công tác
    office_discipline_note TEXT,          -- Ghi chú văn hóa công sở, kỷ cương giờ giấc
    
    priority VARCHAR(20) DEFAULT ''NORMAL'', -- LOW, NORMAL, HIGH, URGENT
    start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    completed_date DATE,
    progress INT DEFAULT 0, -- 0 đến 100%
    status VARCHAR(30) DEFAULT ''IN_PROGRESS'', -- TODO, IN_PROGRESS, COMPLETED, OVERDUE
    evidence_text TEXT,
    weight DOUBLE DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_assignee FOREIGN KEY (assigned_to) REFERENCES users(id),
    CONSTRAINT fk_task_assigner FOREIGN KEY (assigned_by) REFERENCES users(id),
    CONSTRAINT fk_task_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_task_criteria FOREIGN KEY (criteria_id) REFERENCES kpi_criteria(id)
);

-- 6. Nhật ký công việc hằng ngày & Báo cáo tuần (Tổng hợp ngày, tuần, tháng)
CREATE TABLE IF NOT EXISTS work_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id BIGINT,
    log_date DATE NOT NULL,
    week_number INT NOT NULL,
    month_val VARCHAR(20) NOT NULL,
    work_content TEXT NOT NULL,
    hours_spent DOUBLE DEFAULT 8.0,
    result_status VARCHAR(50), -- HOAN_THANH, DANG_XU_LY, VUONG_MAC
    supervisor_comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_log_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL
);

-- 7. Đánh giá Cán bộ cá nhân (Tháng, Quý, Năm)
CREATE TABLE IF NOT EXISTS evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    period_type VARCHAR(20) NOT NULL, -- MONTH, QUARTER, YEAR
    period_value VARCHAR(50) NOT NULL, -- VD: ''Tháng 10/2026''
    self_score DOUBLE DEFAULT 0.0,
    manager_score DOUBLE DEFAULT 0.0,
    final_score DOUBLE DEFAULT 0.0,
    ranking VARCHAR(50), -- XUAT_SAC, TOT, HOAN_THANH, KHONG_HOAN_THANH
    self_notes TEXT,
    manager_notes TEXT,
    leader_notes TEXT,
    status VARCHAR(30) DEFAULT ''DRAFT'', -- DRAFT, SUBMITTED, REVIEWED, APPROVED
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_by BIGINT,
    approved_at TIMESTAMP,
    CONSTRAINT fk_eval_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_eval_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_eval_approver FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- 8. Ban Tổ chức đánh giá Phòng chuyên môn (Tập thể)
CREATE TABLE IF NOT EXISTS department_evaluations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    period_type VARCHAR(20) NOT NULL, -- MONTH, QUARTER, YEAR
    period_value VARCHAR(50) NOT NULL,
    plan_score DOUBLE DEFAULT 0.0,         -- Điểm tiến độ kế hoạch của phòng (30đ)
    quality_score DOUBLE DEFAULT 0.0,      -- Điểm chất lượng tham mưu đề án/văn bản (30đ)
    it_digital_score DOUBLE DEFAULT 0.0,   -- Điểm ứng dụng CNTT, văn phòng số (15đ)
    discipline_score DOUBLE DEFAULT 0.0,   -- Điểm kỷ cương, đoàn kết nội bộ phòng (15đ)
    avg_staff_score DOUBLE DEFAULT 0.0,    -- Điểm trung bình các chuyên viên trong phòng (10đ)
    total_score DOUBLE DEFAULT 0.0,        -- Tổng điểm phòng (thang 100)
    ranking VARCHAR(50),                   -- XUAT_SAC, TOT, HOAN_THANH, KHONG_HOAN_THANH
    leader_feedback TEXT,                  -- Ý kiến kết luận của Lãnh đạo Ban
    evaluated_by BIGINT,
    evaluated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dept_eval_dept FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_dept_eval_leader FOREIGN KEY (evaluated_by) REFERENCES users(id)
);

-- 9. Chi tiết chấm điểm từng tiêu chí
CREATE TABLE IF NOT EXISTS evaluation_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id BIGINT NOT NULL,
    criteria_id BIGINT NOT NULL,
    self_points DOUBLE DEFAULT 0.0,
    manager_points DOUBLE DEFAULT 0.0,
    final_points DOUBLE DEFAULT 0.0,
    notes TEXT,
    CONSTRAINT fk_detail_eval FOREIGN KEY (evaluation_id) REFERENCES evaluations(id) ON DELETE CASCADE,
    CONSTRAINT fk_detail_crit FOREIGN KEY (criteria_id) REFERENCES kpi_criteria(id)
);

-- 10. Nhật ký kiểm toán an toàn dữ liệu (Audit Logs)
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(100),
    action VARCHAR(50) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chỉ mục tối ưu
CREATE INDEX IF NOT EXISTS idx_tasks_type ON tasks(task_type);
CREATE INDEX IF NOT EXISTS idx_worklogs_date ON work_logs(log_date);
CREATE INDEX IF NOT EXISTS idx_worklogs_week ON work_logs(week_number);
