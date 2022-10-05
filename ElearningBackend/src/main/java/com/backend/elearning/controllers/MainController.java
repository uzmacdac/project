package com.backend.elearning.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.elearning.fileupload.FileResponce;
import com.backend.elearning.models.ApproveData;
import com.backend.elearning.models.Aproove;
import com.backend.elearning.models.Category;
import com.backend.elearning.models.ChaptersData;
import com.backend.elearning.models.Course;
import com.backend.elearning.models.CourseByCatIdData;
import com.backend.elearning.models.CourseCategory;
import com.backend.elearning.models.CourseData;
import com.backend.elearning.models.EnrollDetails;
import com.backend.elearning.models.EnrolledCourseData;
import com.backend.elearning.models.EnrolledCourses;
import com.backend.elearning.models.Order;
import com.backend.elearning.models.OrderData;
import com.backend.elearning.models.SubTopic;
import com.backend.elearning.models.SubTopicData;
import com.backend.elearning.models.Topic;
import com.backend.elearning.models.User;
import com.backend.elearning.models.UserData;
import com.backend.elearning.repositories.AprooveRepository;
import com.backend.elearning.repositories.ChapterRepository;
import com.backend.elearning.repositories.CourseRepository;
import com.backend.elearning.repositories.EnrollDetailsRepository;
import com.backend.elearning.repositories.EnrolledCourseRepository;
import com.backend.elearning.repositories.OrderRepository;
import com.backend.elearning.repositories.RegisterRepository;
import com.backend.elearning.services.ApproveService;
import com.backend.elearning.services.CourseService;
import com.backend.elearning.services.EmailSenderService;
import com.backend.elearning.services.EnrolledCoursesService;
import com.backend.elearning.services.FileService;

import com.backend.elearning.services.InstructorService;
import com.backend.elearning.services.OrderService;
import com.backend.elearning.services.RegisterService;
import com.backend.elearning.services.StudentService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/elearning")
public class MainController {

	@Autowired
	private RegisterRepository registerRepository;

	@Autowired
	private CourseRepository courseRepository;

	@Autowired
	private RegisterService registerService;


	@Autowired
	private CourseService courseService;

	@Autowired
	private AprooveRepository aprooveRepository;

	

	

	@PostMapping("/register")
	public String userRegistration(@RequestBody User userToRegister) {
		System.out.println("request hit" + userToRegister.toString());
		return registerService.register(userToRegister);
	}

	@PostMapping("/login")
	public String userLogin(@RequestBody User obj) {
		System.out.println("inside login");
		String username = obj.getUserName();
		String uname = registerService.getUserName(username);
		System.out.println(username + " " + uname);

//		LoginInfo loginInfo = new LoginInfo();

		// password
		if (uname == null) {

//			loginInfo.setMessage("Username not found, Please Register First!");
			return "";
		} else {

			if (uname.equals(username)) {
				String pass = obj.getPass();
				String pwd = registerService.getPassword(uname);

				if (pwd == null) {
//					loginInfo.setMessage("Incorrect Password");
					return "";
				}

				if (pwd.equals(pass)) {
					// get role id
					Integer roleid = registerService.getUserRoleId(uname);

//					loginInfo.setRoleId(roleid);
//					loginInfo.setUserName(uname);
//					loginInfo.setMessage("Login Sucessfull");

					return uname;
				} else {

//					loginInfo.setMessage("Incorrect Password");
					return "";
				}

			} else {
//				loginInfo.setMessage("Incorrect Password");
				return "";
			}
		}
	}

	// get rolename by username
	@PostMapping("/rolename")
	public String getUserRoleName(@RequestBody User user) {
		System.out.println("rolename=" + user.getUserName());
		return registerService.getUserRoleName(user.getUserName());
	}

	@PostMapping("/user/getuid/{userInfo}")
	public String getUserIdByUserName(@PathVariable String userInfo) {

		String result = registerService.getUserIDByUserName(userInfo);

		return result;
	}

	@PostMapping("/getuid")
	public String getUserIdByUserName(@RequestBody User user) {
		System.out.println("username=" + user.getUserName());
		String result = registerService.getUserIDByUserName(user.getUserName());
		System.out.println(result);
		return result;
	}


	
	

	// =================== ADMIN FUNCTIONALITY =========================

//	--------------- Manage Users --------------------------------

	@GetMapping("/admin/users")
	public List<UserData> getAllUsers() {

		List<UserData> userList = new ArrayList<UserData>();

		List<User> uList = registerService.getAllUsersList();

		for (User us : uList) {

			UserData u = new UserData();

			u.setUserId(us.getUserId());
			u.setUserName(us.getUserName());
			u.setFirstName(us.getFirstName());
			u.setLastName(us.getLastName());
			u.setPhoneNo(us.getPhoneNo());
			u.setEmail(us.getEmail());

			userList.add(u);
		}

		return userList;

	}

	@DeleteMapping("admin/deleteuser/{id}")
	public String deleteUser(@PathVariable Long id) {
		registerService.deleteUserByID(id);

		return "User with id " + id + " has been deleted successfully.";
	}

	@GetMapping("/admin/inctruct/count")
	public String getTotalInstrutors() {
		return registerRepository.getUserCount(2);
	}

	@GetMapping("/admin/students/count")
	public String getTotalStudents() {
		return registerRepository.getUserCount(3);
	}

	@GetMapping("/admin/totalcourscnt/")
	public String getTotalCourses() {
		return courseRepository.getCourseCount();
	}
	
	
//	--------------- Manage Users End --------------------------------

//	--------------- Manage Course --------------------------------

	@GetMapping("/admin/courses")
	public List<CourseData> getPlantsList() {

		List<CourseData> cList = new ArrayList<CourseData>();

		List<Course> cl = courseRepository.findAll();

		for (Course course : cl) {

			CourseData cd = new CourseData();

			cd.setCourseId(course.getCourseId());
			cd.setCourseTitle(course.getCourseTitle());
			cd.setCourseDesc(course.getCourseDesc());
			cd.setCourseType(course.getCourseType());
			cd.setCoursePrice(course.getCoursePrice());
			cd.setCourseCategory(course.getCourseCategory().getCourseCatId());
			cd.setUser(course.getUser().getUserId());

			cList.add(cd);

		}

		return cList;

	}

	// delete
	@DeleteMapping("/admin/deleteplant/{id}")
	public String deletePlant(@PathVariable Long id) {
		return courseService.deleteCourseByID(id);
	}

	@PutMapping("/admin/approveuser")
	public String setApproveRequest(@RequestBody Aproove approve) {

		aprooveRepository.save(approve);
		return "User approved sucessfully.";
	}

	@GetMapping("/admin/aprovestatus/{uid}")
	public String getApproveStatus(@PathVariable Long uid) {
		return aprooveRepository.findUserStatus(uid);

	}

//	--------------- Manage Course End --------------------------------

	
	// ------------------------------------------------------------------------

}
