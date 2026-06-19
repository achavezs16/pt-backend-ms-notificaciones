package cl.pymetrack.msnotificaciones.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api-key:}")
    private String apiKey;

    @Value("${sendgrid.from-email:}")
    private String fromEmail;

    @Value("${sendgrid.from-name:PymeTrack}")
    private String fromName;

    @Value("${sendgrid.enabled:true}")
    private boolean enabled;

    public boolean enviarCorreo(String toEmail, String subject, String htmlContent) {
        if (!enabled) {
            System.out.println("📧 SendGrid desactivado. Simulando envío a: " + toEmail);
            return true;
        }

        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("⚠️ SENDGRID_API_KEY no configurada. No se envía correo real.");
            return false;
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            System.out.println("⚠️ SENDGRID_FROM_EMAIL no configurado. No se envía correo real.");
            return false;
        }

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("📨 SendGrid status: " + response.getStatusCode());

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Correo real enviado a: " + toEmail);
                return true;
            }

            System.out.println("⚠️ Error SendGrid body: " + response.getBody());
            return false;

        } catch (IOException e) {
            System.out.println("❌ Error enviando correo con SendGrid: " + e.getMessage());
            return false;
        }
    }
}