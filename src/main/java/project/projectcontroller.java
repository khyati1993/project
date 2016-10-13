package project;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import project.ProfileModel.Profile;
import project.ProfileModel.ProfileService;
import project.ProfileRole.ProfileRoleService;

@Controller
public class projectcontroller {

	@Autowired
	ProfileService us;
	
	@Autowired
	ProfileRoleService urs;
	
	@Autowired
	JavaMailSender mail;
	
	@RequestMapping(value="/")	
	public String home()
	{	
		urs.generateUserRoles();
		return "index";
	}
	@RequestMapping(value="/index")	
	public String index()
	{	
		return "index";
	}
	
	@RequestMapping(value="/searchnewfriend")
	public String searchnewfreind()
	{
		return "searchnewfriend";
	}
	
	@RequestMapping("/profile")
	public ModelAndView prfl() {

		String username = "";
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null && !auth.getName().equals("anonymousUser"))
	    {    
	    	username = auth.getName();
	    	
	    }
	    
		ModelAndView mav = new ModelAndView("profile");

		JSONObject jobj = new JSONObject();
		
		List<Profile> list = us.getAllUsers();
		
		for (Profile p : list) {
			
			if( p.getUsername().equals(username) )
			{
								
				jobj.put("ProfileImage", p.getImage());
				jobj.put("Username", p.getUsername());
				jobj.put("Email", p.getEmail());
				jobj.put("Gender", p.getGender());
				jobj.put("Phone", p.getPhone());
				
				
			}
		}

		mav.addObject("data", jobj.toJSONString());

		return mav;

	}
	
	@RequestMapping(value="/signup")
	public ModelAndView signup()
	{
		 
	 ModelAndView mav = new ModelAndView("signup");
     mav.addObject("newuser",new Profile());	 
     	return mav;
	}


	
	@RequestMapping(value="/insertuser", method = RequestMethod.POST)
	public ModelAndView insertuser( @Valid @ModelAttribute ("newuser") Profile i , BindingResult bind, HttpServletRequest req , HttpServletResponse resp )
	{
		ModelAndView mav = new ModelAndView("signup");
		
		System.out.println(i);
		
		if( bind.hasErrors() )
		{
			mav.addObject("newuser", i);
		}
		else
			
		{
			Profile validateuser = us.getUser(i.getUsername());
			
			if( validateuser == null )
			{
				if( i.getGender().equals("M") )
				{
					i.setImage("resources/images/male.jpg");
				}
				else
				{
					i.setImage("resources/images/female.jpg");
				}
				
			}
			if( i.getPassword().equals(i.getCPassword()) )
			{
				List<Profile> list = us.getAllUsers();
				
				boolean usermatch= false;
				
				for( Profile u : list )
				{
					
					
					if( u.getUsername().equals(i.getUsername()) )
					{
						usermatch= true;
						break;
					}
				}
				
				if( usermatch == false )
				{
					us.insertUser(i);
					
					mav.addObject("newuser", new Profile());
					
					mav.addObject("success", "success");
				}	
				else
				{
					mav.addObject("newuser", i);
					
					mav.addObject("useralreadyexists", "useralreadyexists");
				}
			}
			else
			{	
				mav.addObject("newuser", i);
				
				mav.addObject("passwordmismatch", "passwordmismatch");
			}
			
		}
		
		
		String uemail1 = req.getParameter("email");
		System.out.println( uemail1 );
		
		SimpleMailMessage email = new SimpleMailMessage();
		
		email.setTo(uemail1);
		email.setSubject("Welcome to Krystal Watches");
		email.setText(" Thanks for Contacting Us \n We will get back to you soon \n\n Regards, \n The Krystal Watches Team");
		
		
		try
		{
			mail.send(email);
			
			System.out.println("Mail 2 Sent");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return mav ;
	}
	
		
	@RequestMapping(value="/loginpage" , method = RequestMethod.GET)
	public ModelAndView login() {
		
		ModelAndView mav = new ModelAndView("login");
		
		return mav ;
	}

 
 @RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logoutPage (HttpServletRequest request, HttpServletResponse response) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null){    
	        
	    	System.out.println("In LogOut");
	    	new SecurityContextLogoutHandler().logout(request, response, auth);
	        
	        
	    }
	    
	    return "index";
	}
}