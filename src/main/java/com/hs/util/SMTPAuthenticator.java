package com.hs.util;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public final class SMTPAuthenticator extends Authenticator {
	private String nombreUsuario;
	private String password;
	private boolean necesitaAuenticacion;

	public SMTPAuthenticator(String nombreUsuario, String password,boolean necesitaAutenticacion) {
            this.nombreUsuario = nombreUsuario;
            this.password = password;
            this.necesitaAuenticacion = necesitaAutenticacion;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
            if (this.necesitaAuenticacion) {
                return new PasswordAuthentication(nombreUsuario, password);
            }
            else {
                return null;
            }
	}

}
