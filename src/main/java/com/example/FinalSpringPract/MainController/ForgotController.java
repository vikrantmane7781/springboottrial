package com.example.FinalSpringPract.MainController;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.FinalSpringPract.dao.MainRepository;
import com.example.FinalSpringPract.entity.User;
import com.example.FinalSpringPract.service.EmailService;

@Controller
public class ForgotController {
	@Autowired
	private EmailService eserv;
	
	@Autowired
	private MainRepository mpr; 
	
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
	Random ran=new Random(1000);
	
	@RequestMapping("/forgot")
	public String forgotForm(Model m) {
		m.addAttribute("title","Forgot Pass");
		return "forgotForm";
	}
	
	@PostMapping("/senderotp")
	public String catcherFrom(@RequestParam("email") String email,HttpSession session) {
		
		int otp=ran.nextInt(99999);
		String subject="OTP from SM";
		String message="<h1>OTP ="+otp+"</h1>";
		String to=email;
		
		boolean flag=this.eserv.sendEmail(subject, message, to);
	
		if(flag) {
			session.setAttribute("otpi", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Check your mail id");
			return "forgotForm";
		}
		
	}
	@PostMapping("/mapt")
	public String mager(@RequestParam("otp")Integer otpp,HttpSession session) {
		
		int gtp=(int) session.getAttribute("otpi");
		String email=(String)session.getAttribute("email");
			if(gtp==otpp) {
			System.out.println(gtp);
			
			User user=this.mpr.getUserByName(email);
			if(user==null) {
				session.setAttribute("message", "Check your mail id");
				return "forgot_form";
				}
			else {
				
			}
			return "pass_change";
			}else {
				session.setAttribute("message1", "Wrong otp...");
				return "verify_otp";
			}
	}
	
	@PostMapping("/change-pass")
	public String changePass(@RequestParam("newpass") String newpass,HttpSession session) {
		String email=(String)session.getAttribute("email");
		User user=this.mpr.getUserByName(email);
		user.setPassword(this.bcrypt.encode(newpass));
		this.mpr.save(user);
		
		return "redirect:/logn?change=Password change successfully";
	}
}
