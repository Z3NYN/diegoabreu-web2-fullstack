package br.ueg.trindade.diego_web2_fullstack;

import br.ueg.trindade.diego_web2_fullstack.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.net.ServerSocket;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

class EmailSmtpTests {
    @Test void mensagemEnviadaPorSmtpContemLinkEExpiracao() throws Exception {
        try (ServerSocket servidor = new ServerSocket(0, 1, java.net.InetAddress.getLoopbackAddress());
             ExecutorService executor = Executors.newSingleThreadExecutor()) {
            var recebimento = executor.submit(() -> {
                try (var socket = servidor.accept()) {
                    socket.setSoTimeout(5000);
                    var entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    var saida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
                    saida.print("220 localhost teste SMTP\r\n"); saida.flush();
                    StringBuilder mensagem = new StringBuilder();
                    String linha; boolean dados = false;
                    while ((linha = entrada.readLine()) != null) {
                        if (dados) {
                            if (linha.equals(".")) { dados = false; saida.print("250 Mensagem aceita\r\n"); }
                            else mensagem.append(linha).append("\r\n");
                        } else if (linha.startsWith("DATA")) { dados = true; saida.print("354 Envie dados\r\n"); }
                        else if (linha.startsWith("QUIT")) { saida.print("221 Encerrado\r\n"); saida.flush(); break; }
                        else saida.print("250 OK\r\n");
                        saida.flush();
                    }
                    return mensagem.toString();
                }
            });
            var sender = new JavaMailSenderImpl(); sender.setHost("localhost"); sender.setPort(servidor.getLocalPort());
            sender.setDefaultEncoding("UTF-8");
            sender.getJavaMailProperties().setProperty("mail.smtp.connectiontimeout", "5000");
            sender.getJavaMailProperties().setProperty("mail.smtp.timeout", "5000");
            var factory = new StaticListableBeanFactory(); factory.addBean("sender", sender);
            var service = new EmailService(factory.getBeanProvider(JavaMailSender.class), true, "nexus@example.com", "http://localhost:5173");
            service.enviarConfirmacao("destino@example.com", "token-de-teste");
            var mime = new MimeMessage(Session.getInstance(new Properties()), new ByteArrayInputStream(recebimento.get(10, TimeUnit.SECONDS).getBytes(StandardCharsets.UTF_8)));
            assertEquals("Confirme seu e-mail | Nexus", mime.getSubject());
            assertEquals("destino@example.com", mime.getAllRecipients()[0].toString());
            assertTrue(mime.getContent().toString().contains("http://localhost:5173/?confirmar-email=token-de-teste"));
            assertTrue(mime.getContent().toString().contains("24 horas"));
        }
    }
}
