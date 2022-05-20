package com.example.FinalSpringPract.MainController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.FinalSpringPract.dao.ContactRepository;
import com.example.FinalSpringPract.dao.MainRepository;
import com.example.FinalSpringPract.entity.Contact;
import com.example.FinalSpringPract.entity.User;
import com.example.FinalSpringPract.helper.MsgHelper;


@Controller
@RequestMapping("/user")
public class UserController {
		
	@Autowired
	private BCryptPasswordEncoder bcpy;
	
	@Autowired
	private MainRepository pmr;
	
	@Autowired
	private ContactRepository cpr;
	
	@ModelAttribute
	public void addCommonData(Model m, Principal pri) {
		String username = pri.getName();

		User user = pmr.getUserByName(username);
		
		m.addAttribute("uu", user);
		
		System.out.println(".I am user" + user);
	}

		@RequestMapping("/udash")
		public String userDashBoard(Model m) {
			m.addAttribute("title", "User Dashboard");
			return "user/user_dash";
		}
		
		@RequestMapping("/addc")
		public String addContactRedirect(Model m) {
			m.addAttribute("title","Add Contacts");
			m.addAttribute("contact", new Contact());
			return "user/add_contact_form";
		}
		
		@RequestMapping("/process-contact")
		public String contactAdd(@ModelAttribute Contact contact, @RequestParam("gimage") MultipartFile file,
				Principal pri, HttpSession session) {
			try {
				// System.out.println("Data of ocntact"+contact);
				String name = pri.getName();

				User userName = this.pmr.getUserByName(name);

				// process image file
				if (file.isEmpty()) {
					contact.setImage("download2.jpg");

				} else {
					contact.setImage(file.getOriginalFilename());
					File filer = new ClassPathResource("static/image").getFile();

					Path path = Paths.get(filer.getAbsolutePath() + File.separator + file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

					session.setAttribute("message", new MsgHelper("Contact has been added successfully !!", "success"));
				}

				userName.getCotacts().add(contact);
				contact.setUser(userName);

				this.pmr.save(userName);

			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute("message", new MsgHelper("Something went wrong try agains !!", "success"));
			}
			return "user/add_contact_form";
		}
		
		@RequestMapping("/show-contacts/{page}")
		public String showAllRelatedContacts(@PathVariable("page") int page, Model m, Principal pri) {
			m.addAttribute("title","Contact-Info");
			String userName = pri.getName();
			User uuu = this.pmr.getUserByName(userName);

			Pageable pageable = PageRequest.of(page, 2);

			Page<Contact> contacts = this.cpr.findContactsByUser(uuu.getId(), pageable);

			m.addAttribute("contacts", contacts);
			m.addAttribute("currentpage", page);
			m.addAttribute("totalpage", contacts.getTotalPages());

			System.out.println(contacts);
			return "user/show_contact";
		}
		
		
		///get seperate detail
		@RequestMapping("/contact/{cid}")
		public String showPersonDetail(@PathVariable("cid") Integer cid, Model m, Principal pri) {

			Optional<Contact> contactOpt = this.cpr.findById(cid);
			Contact conta = contactOpt.get();

			// security concern
			String userName = pri.getName();
			User user = this.pmr.getUserByName(userName);

			if (user.getId() == conta.getUser().getId()) {
				m.addAttribute("conta", conta);
				m.addAttribute("title", conta.getName());

			}
			return "user/contactDetails";

		}
		
		@RequestMapping("/dalta/{id}")
		public String deleteSpecificContact(@PathVariable("id") Integer sid, Principal pri) {
			Contact conta = this.cpr.findById(sid).get();

			// conta.setUser(null);
			User user = this.pmr.getUserByName(pri.getName());

			user.getCotacts().remove(conta);

			this.pmr.save(user);
			// this.cpr.delete(conta);
			// this.cpr.deleteById(conta.getCid());
			System.out.println("----------->" + conta.getCid());

			return "redirect:/user/show-contacts/0";
		}
		
		@RequestMapping("/update-contact/{id}")
		public String updateContactsbyId(@PathVariable("id") Integer uids, Principal pri,Model m) {
			m.addAttribute("title", "Update Contact");
			Contact updateContact = this.cpr.findById(uids).get();
			m.addAttribute("contact", updateContact);
			return "user/update-form";
		}
		
		@RequestMapping("/process-contact-update")
		public String getDataAndUpdateData(@ModelAttribute Contact contact, @RequestParam("gimage") MultipartFile file,
				Principal pri, HttpSession session) {
			try {
				Contact oldcontactdetail=this.cpr.findById(contact.getCid()).get();

				if (!file.isEmpty()) {
					//delete old photo
					
					File deleteFile = new ClassPathResource("static/image").getFile();
					File file2=new File(deleteFile,oldcontactdetail.getImage());
					file2.delete();
					
					//update new photo
					
					File filer = new ClassPathResource("static/image").getFile();

					Path path = Paths.get(filer.getAbsolutePath() + File.separator + file.getOriginalFilename());

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
					
					contact.setImage(file.getOriginalFilename());
					
				} else {
					contact.setImage(oldcontactdetail.getImage());
				}
				
				User user=this.pmr.getUserByName(pri.getName());
				contact.setUser(user);
				this.cpr.save(contact);
				
				session.setAttribute("message", new MsgHelper("Contact updated successfully ...", "success"));
				
			}catch(Exception e) {
				e.printStackTrace();
				
			}
			
			
			return "redirect:/user/contact/"+contact.getCid();
		}
		
		@RequestMapping("/dractf")
		public String viewDetails(Model m) {
			m.addAttribute("title","See Profile");
			return "user/drafter";
		}
		
		@RequestMapping("/setting")
		public String processSetting(Model m) {
			m.addAttribute("title","Settings");
			return "user/setting";
		}
		
		@RequestMapping(value="/changepass")
		public String processPassUpdate(@RequestParam("old")String old,@RequestParam("new")String newer, Principal pri,HttpSession session) {
			String username=pri.getName();
			User currentUser=this.pmr.getUserByName(username);
			
			if(this.bcpy.matches(old, currentUser.getPassword())) {
				currentUser.setPassword(this.bcpy.encode(newer));
				this.pmr.save(currentUser);
				session.setAttribute("message", new MsgHelper("Password updated succesfully", "success"));
			}else {
				session.setAttribute("message", new MsgHelper("Please enter correct password", "danger"));
				return"redirect:/user/setting";
			}
			return"redirect:/user/udash";
		}
}
