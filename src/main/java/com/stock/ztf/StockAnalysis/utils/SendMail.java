package com.stock.ztf.StockAnalysis.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * JavaMail发送邮件:前提是QQ邮箱里帐号设置要开启POP3/SMTP协议
 */
public class SendMail {

	public static void main(String[] args) throws Exception {

		sendMail("12341423131313");
	}
	
	public static void sendMail(String msg) throws Exception {

		Properties prop = new Properties();
		// 开启debug调试，以便在控制台查看
		prop.setProperty("mail.debug", "false");
		// 设置邮件服务器主机名
		prop.setProperty("mail.host", "smtp.126.com");
		// 发送服务器需要身份验证
		prop.setProperty("mail.smtp.auth", "true");
		// 发送邮件协议名称
		prop.setProperty("mail.transport.protocol", "smtp");

		// 开启SSL加密，否则会失败
//		MailSSLSocketFactory sf = new MailSSLSocketFactory();
//		sf.setTrustAllHosts(true);
//		prop.put("mail.smtp.ssl.enable", "true");
//		prop.put("mail.smtp.ssl.socketFactory", sf);

		// 创建session
		Session session = Session.getInstance(prop);
		// 通过session得到transport对象
		Transport ts = session.getTransport();
		// 连接邮件服务器：邮箱类型，帐号，授权码代替密码（更安全）
		/**
		 * 后面的字符是授权码，用qq密码反正我是失败了（用自己的，别用我的，这个号是我瞎编的，为了。。。。）
		 */
		ts.connect("smtp.126.com", "zhangtianfeng_2008@126.com", "13227890226cnmwz");
		// 创建邮件
		Message message = createSimpleMail(session,msg);
		// 发送邮件
		ts.sendMessage(message, message.getAllRecipients());
		ts.close();
	}

	/**
	 * @Method: createSimpleMail
	 * @Description: 创建一封只包含文本的邮件
	 */
	public static MimeMessage createSimpleMail(Session session,String msg) throws Exception {
		// 创建邮件对象
		MimeMessage message = new MimeMessage(session);
		// 指明邮件的发件人
		message.setFrom(new InternetAddress("zhangtianfeng_2008@126.com"));
		// 指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
		message.setRecipient(Message.RecipientType.TO, new InternetAddress("zhangtianfeng_2008@126.com"));
		// 邮件的标题
		message.setSubject("gupiao统计数据");
		// 邮件的文本内容
		message.setContent(msg, "text/html;charset=UTF-8");
		// 返回创建好的邮件对象
		return message;
	}
}
