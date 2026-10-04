package vn.btctu.daklak.kpi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KpiApplication {
    public static void main(String[] args) {
        SpringApplication.run(KpiApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" HE THONG KPI BAN TO CHUC TINH UY DAK LAK DA KHOI CHAY!");
        System.out.println(" Trang chu ung dung: http://localhost:8080");
        System.out.println(" H2 SQL Web Console: http://localhost:8080/h2-console");
        System.out.println("==================================================================");
    }
}
