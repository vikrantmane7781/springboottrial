package com.example.FinalSpringPract.MainController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.FinalSpringPract.dao.MainRepository;
import com.example.FinalSpringPract.entity.User;
import com.example.FinalSpringPract.helper.MsgHelper;

@Controller
public class MainController {
	
	@Autowired
	private BCryptPasswordEncoder bcp;
	
	@Autowired
	private MainRepository mr;
	
	@RequestMapping("/")
	public String homeRender(Model m) {
		m.addAttribute("title", "Home Page");
		return "home";
	}
	
	@RequestMapping("/visitSignUp")
	public String redirectToSignPage(Model m) {
		m.addAttribute("title","SignUp Page");
		m.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping(value="/do_regis",method = RequestMethod.POST)
	public String processRegistration(@Valid @ModelAttribute("user") User user,
			@RequestParam("gimage") MultipartFile file,BindingResult res,
			@RequestParam(value = "agreement", 
			defaultValue = "false") boolean agreement, 
			Model m,
			HttpSession session) {
		
			m.addAttribute("title","SignUp Page");
			//--
			try {

				if (!agreement) {
					throw new Exception("Accept terms & condition");
				}
				if(file.isEmpty()) {
					throw new Exception("Please select image..");
				}
				 if(res.hasErrors()) {
					m.addAttribute("user",user);
					return "signup";
				}
				 
				    user.setImgUrl(file.getOriginalFilename());
					File filer = new ClassPathResource("static/image").getFile();

					Path path = Paths.get(filer.getAbsolutePath() + File.separator + file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				 
				user.setRole("ROLE_USER");
				user.setEnabled(true);
				user.setPassword(bcp.encode(user.getPassword()));

				User us = this.mr.save(user);
				m.addAttribute("us", us);

				System.out.println("Aggrement " + agreement);
				System.out.println("User" + user);

				m.addAttribute("user", new User());

				session.setAttribute("message", new MsgHelper("Successed", "alert-success"));
				System.out.println(session);
				return "signup";

			} catch (Exception e) {
				e.printStackTrace();
				m.addAttribute("user", user);
				session.setAttribute("message", new MsgHelper("Something went wrong..." + e.getMessage(), "alert-error"));
				System.out.println(session);
				return "signup";
			}
			
	}
	
	@RequestMapping("/log")
	public String logRedirect(Model m) {
		m.addAttribute("title","Login page");
		return "login";
	}
}
