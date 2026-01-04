package com.novibe;


import com.novibe.common.DnsTaskRunner;
import com.novibe.common.config.EnvironmentVariables;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


public class App {
    static void main() {
        String provider = EnvironmentVariables.DNS.toUpperCase();

        String commonsBasePackage = "com.novibe.common";

        String dnsBasePackage = switch (provider) {
            case "CLOUDFLARE" -> "com.novibe.dns.cloudflare";
            case "NEXTDNS" -> "com.novibe.dns.next_dns";
            default -> throw new IllegalArgumentException("Unsupported DNS provider! Must be CLOUDFLARE or NEXTDNS");
        };

        AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext(dnsBasePackage, commonsBasePackage);

        DnsTaskRunner runner = context.getBean(DnsTaskRunner.class);
        runner.run();
    }
}
