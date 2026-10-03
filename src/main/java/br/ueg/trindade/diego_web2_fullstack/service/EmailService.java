package br.ueg.trindade.diego_web2_fullstack.service;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
@Service
public class EmailService {
    private final ObjectProvider<JavaMailSender> sender;
    private final boolean habilitado;
    private final String remetente;
    private final String frontendUrl;
    public EmailService(ObjectProvider<JavaMailSender> sender,
        @Value("${nexus.email.enabled:false}") boolean habilitado,
        @Value("${nexus.email.from:}") String remetente,
        @Value("${nexus.frontend-url:http://localhost:5173}") String frontendUrl) {
        this.sender = sender; this.habilitado = habilitado; this.remetente = remetente; this.frontendUrl = frontendUrl;
    }
    public boolean habilitado() { return habilitado; }
    public void enviarConfirmacao(String destinatario, String token) {
        JavaMailSender mail = sender.getIfAvailable();
        if (!habilitado || mail == null || remetente.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O envio de e-mails ainda não está configurado. Contate o administrador.");
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente); mensagem.setTo(destinatario);
        mensagem.setSubject("Confirme seu e-mail | Nexus");
        mensagem.setText("Olá!\n\nConfirme seu endereço de e-mail na Nexus pelo link:\n" + frontendUrl + "/?confirmar-email=" + token
            + "\n\nO link expira em 24 horas e só pode ser usado uma vez. Se não solicitou este cadastro, ignore esta mensagem.\n\nNexus | Gestão de cadastros");
        try { mail.send(mensagem); }
        catch (MailException ex) { throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível enviar o e-mail de confirmação. Confira a configuração de envio e tente novamente."); }
    }
}
