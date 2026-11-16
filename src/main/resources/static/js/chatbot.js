/**
 * TRỢ LÝ ẢO AI NGHIỆP VỤ KPI NỘI BỘ - BAN TỔ CHỨC TỈNH ỦY ĐẮK LẮK
 * Tích hợp Tường lửa Chống Web LLM Attacks & Cô lập ngữ cảnh theo 5 Cấp độ (RBAC Context Isolation)
 */
(function () {
    function initChatbotWidget() {
        if (document.getElementById('btctuAiChatbotRoot')) return;

        const style = document.createElement('style');
        style.textContent = `
            #btctuAiChatbotRoot {
                position: fixed;
                bottom: 24px;
                right: 24px;
                z-index: 99999;
                font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
            }
            .btctu-chat-fab {
                display: flex;
                align-items: center;
                gap: 10px;
                background: linear-gradient(135deg, #b71c1c 0%, #880e4f 100%);
                color: #fff;
                border: 2px solid #ffd54f;
                border-radius: 50px;
                padding: 11px 18px;
                cursor: pointer;
                box-shadow: 0 8px 24px rgba(183, 28, 28, 0.5);
                transition: all 0.25s ease;
                user-select: none;
            }
            .btctu-chat-fab:hover {
                transform: translateY(-3px);
                box-shadow: 0 12px 28px rgba(183, 28, 28, 0.7);
            }
            .btctu-chat-fab-icon {
                width: 36px;
                height: 36px;
                border-radius: 50%;
                background: #ffd54f;
                color: #b71c1c;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 18px;
                font-weight: bold;
                flex-shrink: 0;
            }
            .btctu-chat-fab-text {
                text-align: left;
                line-height: 1.2;
            }
            .btctu-chat-fab-title {
                font-weight: 700;
                font-size: 13.5px;
                color: #fff;
                display: block;
            }
            .btctu-chat-fab-sub {
                font-size: 10.5px;
                color: #ffe082;
                display: block;
            }
            .btctu-chat-window {
                position: fixed;
                bottom: 88px;
                right: 24px;
                width: 410px;
                max-width: calc(100vw - 32px);
                height: 560px;
                max-height: calc(100vh - 120px);
                background: #ffffff;
                border-radius: 16px;
                box-shadow: 0 16px 48px rgba(0, 0, 0, 0.28);
                border: 1px solid rgba(183, 28, 28, 0.25);
                display: none;
                flex-direction: column;
                overflow: hidden;
                animation: btctuChatSlideUp 0.22s ease-out;
            }
            .btctu-chat-window.open {
                display: flex;
            }
            @keyframes btctuChatSlideUp {
                from { opacity: 0; transform: translateY(16px) scale(0.98); }
                to { opacity: 1; transform: translateY(0) scale(1); }
            }
            .btctu-chat-header {
                background: linear-gradient(135deg, #9a0007 0%, #c62828 100%);
                color: #fff;
                padding: 13px 16px;
                display: flex;
                align-items: center;
                justify-content: space-between;
                border-bottom: 2px solid #ffd54f;
            }
            .btctu-chat-header-info {
                display: flex;
                align-items: center;
                gap: 10px;
            }
            .btctu-chat-avatar {
                width: 38px;
                height: 38px;
                border-radius: 50%;
                background: #ffd54f;
                color: #b71c1c;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 19px;
                font-weight: bold;
                flex-shrink: 0;
            }
            .btctu-chat-header-title {
                font-weight: 700;
                font-size: 14px;
                margin: 0;
                line-height: 1.25;
            }
            .btctu-chat-header-badge {
                font-size: 10.5px;
                color: #ffd54f;
                display: flex;
                align-items: center;
                gap: 5px;
                margin-top: 2px;
            }
            .btctu-dot-online {
                width: 7px;
                height: 7px;
                border-radius: 50%;
                background: #4caf50;
                display: inline-block;
                box-shadow: 0 0 6px #4caf50;
            }
            .btctu-chat-close-btn {
                background: rgba(255,255,255,0.15);
                border: none;
                color: #fff;
                width: 30px;
                height: 30px;
                border-radius: 50%;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 14px;
                transition: background 0.2s;
            }
            .btctu-chat-close-btn:hover {
                background: rgba(255,255,255,0.3);
            }
            .btctu-security-banner {
                background: #fff8e1;
                color: #880e4f;
                font-size: 11px;
                font-weight: 600;
                padding: 6px 12px;
                border-bottom: 1px solid #ffe082;
                display: flex;
                align-items: center;
                justify-content: space-between;
            }
            .btctu-chat-messages {
                flex: 1;
                padding: 14px;
                overflow-y: auto;
                background: #f8f9fa;
                display: flex;
                flex-direction: column;
                gap: 12px;
            }
            .btctu-msg {
                max-width: 88%;
                padding: 10px 13px;
                border-radius: 13px;
                font-size: 13.2px;
                line-height: 1.5;
                word-wrap: break-word;
            }
            .btctu-msg-bot {
                align-self: flex-start;
                background: #ffffff;
                color: #1e293b;
                border: 1px solid #e2e8f0;
                border-top-left-radius: 4px;
                box-shadow: 0 2px 5px rgba(0,0,0,0.04);
            }
            .btctu-msg-user {
                align-self: flex-end;
                background: #b71c1c;
                color: #ffffff;
                border-bottom-right-radius: 4px;
                box-shadow: 0 2px 6px rgba(183, 28, 28, 0.2);
            }
            .btctu-msg-bot code {
                background: #fff3e0;
                color: #b71c1c;
                padding: 1px 5px;
                border-radius: 4px;
                font-weight: 600;
                font-size: 12.2px;
            }
            .btctu-chat-suggestions {
                padding: 8px 12px;
                background: #fff;
                border-top: 1px solid #edf2f7;
                display: flex;
                flex-wrap: wrap;
                gap: 6px;
                max-height: 82px;
                overflow-y: auto;
            }
            .btctu-chip {
                background: #fff8e1;
                color: #9a0007;
                border: 1px solid #ffe082;
                border-radius: 20px;
                padding: 4px 10px;
                font-size: 11.5px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.15s;
                white-space: nowrap;
            }
            .btctu-chip:hover {
                background: #b71c1c;
                color: #fff;
                border-color: #b71c1c;
            }
            .btctu-chat-input-bar {
                padding: 10px 12px;
                background: #fff;
                border-top: 1px solid #e2e8f0;
                display: flex;
                gap: 8px;
                align-items: center;
            }
            .btctu-chat-input {
                flex: 1;
                border: 1px solid #cbd5e1;
                border-radius: 22px;
                padding: 8px 14px;
                font-size: 13px;
                outline: none;
                transition: border-color 0.2s;
            }
            .btctu-chat-input:focus {
                border-color: #b71c1c;
            }
            .btctu-chat-send {
                background: #b71c1c;
                color: #ffd54f;
                border: none;
                width: 36px;
                height: 36px;
                border-radius: 50%;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 14px;
                flex-shrink: 0;
                transition: transform 0.15s, background 0.2s;
            }
            .btctu-chat-send:hover {
                background: #880e4f;
                transform: scale(1.05);
            }
            .btctu-typing {
                font-size: 12px;
                color: #64748b;
                font-style: italic;
                padding: 4px 10px;
            }
        `;
        document.head.appendChild(style);

        const root = document.createElement('div');
        root.id = 'btctuAiChatbotRoot';
        root.innerHTML = `
            <div class="btctu-chat-window" id="btctuChatWindow">
                <div class="btctu-chat-header">
                    <div class="btctu-chat-header-info">
                        <div class="btctu-chat-avatar"><i class="fa-solid fa-robot"></i></div>
                        <div>
                            <div class="btctu-chat-header-title">Trợ lý AI Nghiệp vụ KPI</div>
                            <div class="btctu-chat-header-badge">
                                <span class="btctu-dot-online"></span>
                                <span id="btctuModeLabel">Chống Web LLM Attacks • Cấp độ 3</span>
                            </div>
                        </div>
                    </div>
                    <div style="display:flex;gap:6px;align-items:center;">
                        <button type="button" class="btctu-chat-close-btn" id="btctuChatKeyBtn" style="display:none;" title="Cấu hình API Key (Chỉ dành cho Admin / Cấp 1)">
                            <i class="fa-solid fa-key"></i>
                        </button>
                        <button type="button" class="btctu-chat-close-btn" id="btctuChatClose" title="Thu nhỏ">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    </div>
                </div>
                <div class="btctu-security-banner">
                    <span id="btctuScopeBadge"><i class="fa-solid fa-shield-halved me-1"></i> Cô lập ngữ cảnh theo chức vụ</span>
                    <span class="badge bg-danger" style="font-size:10px;">RBAC Guard</span>
                </div>
                <div id="btctuApiKeyBox" style="display:none;background:#fff8e1;padding:10px 12px;border-bottom:1px solid #ffe082;">
                    <div style="font-size:11.5px;font-weight:700;color:#b71c1c;margin-bottom:5px;">
                        <i class="fa-solid fa-key me-1"></i> Cấu hình API Key (Chỉ Admin / Lãnh đạo Cấp 1):
                    </div>
                    <div style="display:flex;gap:6px;">
                        <input type="password" id="btctuApiKeyInput" placeholder="Dán API Key (Gemini / OpenAI / Groq)"
                               style="flex:1;border:1px solid #cbd5e1;border-radius:6px;padding:5px 8px;font-size:12px;">
                        <button type="button" id="btctuApiKeySave"
                                style="background:#b71c1c;color:#fff;border:none;border-radius:6px;padding:5px 10px;font-size:12px;font-weight:600;cursor:pointer;">
                            Lưu
                        </button>
                    </div>
                </div>
                <div class="btctu-chat-messages" id="btctuChatMessages"></div>
                <div class="btctu-chat-suggestions" id="btctuChatSuggestions"></div>
                <form class="btctu-chat-input-bar" id="btctuChatForm">
                    <input type="text" class="btctu-chat-input" id="btctuChatInput"
                           placeholder="Nhập câu hỏi trong phạm vi cấp độ của đồng chí..." autocomplete="off">
                    <button type="submit" class="btctu-chat-send" title="Gửi câu hỏi">
                        <i class="fa-solid fa-paper-plane"></i>
                    </button>
                </form>
            </div>
            <button type="button" class="btctu-chat-fab" id="btctuChatFab">
                <div class="btctu-chat-fab-icon"><i class="fa-solid fa-robot"></i></div>
                <div class="btctu-chat-fab-text">
                    <span class="btctu-chat-fab-title">Trợ lý AI Nghiệp vụ</span>
                    <span class="btctu-chat-fab-sub" id="btctuFabSub">Bảo mật phân cấp Cấp độ 3</span>
                </div>
            </button>
        `;
        document.body.appendChild(root);

        // Bổ sung nút "Trợ lý AI" trực tiếp lên thanh Menu chính (Navbar) để dễ nhìn thấy ngay lập tức
        const navList = document.querySelector('.navbar-nav.me-auto');
        if (navList && !document.getElementById('navAiChatbotBtn')) {
            const li = document.createElement('li');
            li.className = 'nav-item';
            li.innerHTML = `
                <a class="nav-link fw-bold text-warning" href="javascript:void(0)" id="navAiChatbotBtn">
                    <i class="fa-solid fa-robot me-1"></i> Trợ lý AI
                </a>
            `;
            navList.appendChild(li);
            li.querySelector('#navAiChatbotBtn').addEventListener('click', () => {
                toggleChatWindow(true);
            });
        }

        const chatWindow = document.getElementById('btctuChatWindow');
        const chatFab = document.getElementById('btctuChatFab');
        const chatClose = document.getElementById('btctuChatClose');
        const chatKeyBtn = document.getElementById('btctuChatKeyBtn');
        const apiKeyBox = document.getElementById('btctuApiKeyBox');
        const apiKeyInput = document.getElementById('btctuApiKeyInput');
        const apiKeySave = document.getElementById('btctuApiKeySave');
        const modeLabel = document.getElementById('btctuModeLabel');
        const scopeBadge = document.getElementById('btctuScopeBadge');
        const fabSub = document.getElementById('btctuFabSub');
        const chatMessages = document.getElementById('btctuChatMessages');
        const chatSuggestions = document.getElementById('btctuChatSuggestions');
        const chatForm = document.getElementById('btctuChatForm');
        const chatInput = document.getElementById('btctuChatInput');

        let initialized = false;

        function toggleChatWindow(forceOpen) {
            if (forceOpen === true) {
                chatWindow.classList.add('open');
            } else {
                chatWindow.classList.toggle('open');
            }
            if (chatWindow.classList.contains('open')) {
                loadInitGreeting();
                setTimeout(() => chatInput.focus(), 120);
            }
        }

        chatKeyBtn.addEventListener('click', () => {
            apiKeyBox.style.display = apiKeyBox.style.display === 'none' ? 'block' : 'none';
            if (apiKeyBox.style.display === 'block') apiKeyInput.focus();
        });

        apiKeySave.addEventListener('click', async () => {
            const keyVal = apiKeyInput.value.trim();
            try {
                const res = await fetch('/api/chatbot/config-key', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json; charset=UTF-8' },
                    credentials: 'same-origin',
                    body: JSON.stringify({ apiKey: keyVal })
                });
                const data = await res.json();
                apiKeyBox.style.display = 'none';
                appendMessage(data.message || 'Đã cập nhật cấu hình.', false);
            } catch (e) {
                appendMessage('Không thể lưu API Key. Vui lòng thử lại.', false);
            }
        });

        function formatMarkdown(md) {
            if (!md) return '';
            let html = md
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;');
            html = html.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>');
            html = html.replace(/\*([^*]+)\*/g, '<em>$1</em>');
            html = html.replace(/`([^`]+)`/g, '<code>$1</code>');
            html = html.replace(/\n/g, '<br>');
            return html;
        }

        function appendMessage(text, isUser) {
            const div = document.createElement('div');
            div.className = 'btctu-msg ' + (isUser ? 'btctu-msg-user' : 'btctu-msg-bot');
            if (isUser) {
                div.textContent = text;
            } else {
                div.innerHTML = formatMarkdown(text);
            }
            chatMessages.appendChild(div);
            chatMessages.scrollTop = chatMessages.scrollHeight;
        }

        function renderSuggestions(chips) {
            chatSuggestions.innerHTML = '';
            if (!Array.isArray(chips)) return;
            chips.forEach(label => {
                const btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'btctu-chip';
                btn.textContent = label;
                btn.addEventListener('click', () => sendQuestion(label));
                chatSuggestions.appendChild(btn);
            });
        }

        async function loadInitGreeting() {
            if (initialized) return;
            initialized = true;
            try {
                const res = await fetch('/api/chatbot/init', { credentials: 'same-origin' });
                if (res.ok) {
                    const data = await res.json();
                    if (data.orgLevel) {
                        scopeBadge.innerHTML = '<i class="fa-solid fa-lock me-1"></i> Cô lập ngữ cảnh: Cấp ' + data.orgLevel + ' (' + data.roleBadge + ')';
                        fabSub.textContent = 'Cấp ' + data.orgLevel + ' • ' + data.roleBadge;
                    }
                    if (data.isAdmin || data.orgLevel === 1) {
                        chatKeyBtn.style.display = 'flex';
                    }
                    modeLabel.textContent = data.hasApiKey
                        ? 'Hybrid AI • Chống Web LLM Attacks'
                        : 'On-Premise Cấp độ 3 • RBAC Isolated';
                    appendMessage(data.greeting, false);
                    renderSuggestions(data.suggestions);
                } else {
                    appendMessage('Xin chào đồng chí! Tôi là **Trợ lý AI Nghiệp vụ KPI — Ban Tổ chức Tỉnh ủy Đắk Lắk**.', false);
                }
            } catch (e) {
                appendMessage('Xin chào đồng chí! Tôi là **Trợ lý AI Nghiệp vụ KPI — Ban Tổ chức Tỉnh ủy Đắk Lắk**.', false);
            }
        }

        async function sendQuestion(questionText) {
            const q = (questionText || '').trim();
            if (!q) return;
            appendMessage(q, true);
            chatInput.value = '';

            const typing = document.createElement('div');
            typing.className = 'btctu-typing';
            typing.innerHTML = '<i class="fa-solid fa-spinner fa-spin me-1"></i> Đang kiểm tra phân quyền RBAC & tra cứu...';
            chatMessages.appendChild(typing);
            chatMessages.scrollTop = chatMessages.scrollHeight;

            try {
                const res = await fetch('/api/chatbot/ask', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json; charset=UTF-8' },
                    credentials: 'same-origin',
                    body: JSON.stringify({ question: q })
                });
                typing.remove();
                if (res.ok) {
                    const data = await res.json();
                    appendMessage(data.reply, false);
                    if (data.suggestions) {
                        renderSuggestions(data.suggestions);
                    }
                } else {
                    appendMessage('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.', false);
                }
            } catch (err) {
                typing.remove();
                appendMessage('Không thể kết nối tới máy chủ nội bộ.', false);
            }
        }

        chatFab.addEventListener('click', () => toggleChatWindow());
        chatClose.addEventListener('click', () => chatWindow.classList.remove('open'));
        chatForm.addEventListener('submit', (e) => {
            e.preventDefault();
            sendQuestion(chatInput.value);
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initChatbotWidget);
    } else {
        initChatbotWidget();
    }
})();
