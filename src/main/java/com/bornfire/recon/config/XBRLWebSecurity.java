package com.bornfire.recon.config;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.transaction.annotation.Transactional;
import com.bornfire.recon.entities.BRECON_Audit_Entity;
import com.bornfire.recon.entities.BRECON_AUDIT_REPO;
import com.bornfire.recon.entities.USER_PROFILE_ENTITY;
import com.bornfire.recon.entities.USER_PROFILE_REPO;
import com.bornfire.recon.services.LoginServices;

@Configuration
@EnableWebSecurity
public class XBRLWebSecurity extends WebSecurityConfigurerAdapter {

	@Autowired
	USER_PROFILE_REPO userProfileRep;

	@Autowired
	SessionFactory sessionFactory;
	
	@Autowired
	LoginServices loginServices;
	
	@Autowired
	BRECON_AUDIT_REPO AuditRepo;
	
	@Autowired
	SequenceGenerator sequence;

	private static final Logger logger = LoggerFactory.getLogger(XBRLWebSecurity.class);

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests().antMatchers("/css/**","/js/**",  "/webfonts/**",  "/images/**", "/login*", "/freezeColumn/**","favicon.ico").permitAll()
				.anyRequest().authenticated().and().formLogin().loginPage("/login").permitAll()
				.failureHandler(xbrlAuthFailHandle()).successHandler(xbrlAuthSuccessHandle())
				.usernameParameter("userid").and().logout().permitAll().and()
				.logout().logoutSuccessHandler(xbrlLogoutSuccessHandler()).permitAll()
				.and().sessionManagement().maximumSessions(1)
				.maxSessionsPreventsLogin(false);
				

		http.csrf().disable();

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Bean

	public AuthenticationProvider authenticationProvider() {

		DaoAuthenticationProvider ap = new DaoAuthenticationProvider() {

			@Override
			@Transactional
			public Authentication authenticate(Authentication authentication) throws AuthenticationException {
				String userid = authentication.getName();
				String password = authentication.getCredentials().toString();

				Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(userid);

				try {

					if (up.isPresent()) {
						USER_PROFILE_ENTITY usr = up.get();

						if (!usr.isAccountNonExpired()) {

							throw new AccountExpiredException("Account Expired");

						} else if (!usr.isCredentialsNonExpired()) {

							throw new CredentialsExpiredException("Credentials Expired");

						} else if (!usr.isAccountNonLocked()) {

							throw new LockedException("Account Locked");

						} else if (!usr.isEnabled()) {

							throw new DisabledException("Account Disabled");

						} else if (!usr.isLoginAllowed()) {

							throw new LockedException("Login Not Allowed");

						} else if (!PasswordEncryption.validatePassword(password, usr.getPassword())) {

							logger.info("Passing Userid :" + userid);

							Session hs = sessionFactory.getCurrentSession();
							Transaction tr = hs.getTransaction();
							hs.createQuery(
									"update BRECON_USER_PROFILE_TABLE a set a.no_of_attmp=nvl(a.no_of_attmp,0)+1, a.user_locked_flg=decode(nvl(a.no_of_attmp,0)+1,'3','Y','N'), a.login_status=decode(nvl(a.no_of_attmp,0)+1,'3','Inactive','Active') where userid=?1")
									.setParameter(1, userid).executeUpdate();
							tr.commit();
							hs.close();
							throw new BadCredentialsException("Authentication failed");

						} else {
							
							return new UsernamePasswordAuthenticationToken(userid, password, Collections.emptyList());

						}

					} else {

						throw new UsernameNotFoundException("Invalid User Name");
					}

				} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
					e.printStackTrace();
					authentication.setAuthenticated(false);
				}
				return authentication;

			}

			@Override
			public boolean supports(Class<?> aClass) {
				return aClass.equals(UsernamePasswordAuthenticationToken.class);
			}

		};

		ap.setHideUserNotFoundExceptions(false);
		ap.setUserDetailsService(userDetailsService());

		return ap;
	}

	@Bean
	@Override
	public UserDetailsService userDetailsService() {

		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

				Optional<USER_PROFILE_ENTITY> up = userProfileRep.findByusername(username);

				if (up.isPresent()) {
					return up.get();
				} else {
					return new USER_PROFILE_ENTITY();
				}

			}

		};
	}

	@Bean
	public AuthenticationFailureHandler xbrlAuthFailHandle() {
		return new AuthenticationFailureHandler() {

			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
					AuthenticationException exception) throws IOException, ServletException {

				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				logger.info(exception.getMessage());
				response.sendRedirect("login?error=" + exception.getMessage());

			}

		};

	}

	@Bean
	public AuthenticationSuccessHandler xbrlAuthSuccessHandle() {
		return new AuthenticationSuccessHandler() {

			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				String auditID = sequence.generateRequestUUId();
				
				Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(authentication.getName());
				USER_PROFILE_ENTITY user = up.get();
				user.setNo_of_attmp(0);
				user.setUser_locked_flg("N");
				userProfileRep.save(user);
				
				loginServices.SessionLogging("LOGIN","M1",request.getSession().getId(),user.getUserid(),request.getRemoteAddr(),
						"ACTIVE");
			
				
				request.getSession().setAttribute("USERID", user.getUserid());
				request.getSession().setAttribute("USERNAME", user.getUsername());
				request.getSession().setAttribute("ROLEID", user.getRole_id());
				request.getSession().setAttribute("DOMAINID", user.getDomain_id());
				request.getSession().setAttribute("PERMISSIONS", user.getPermissions());
				request.getSession().setAttribute("WORKCLASS", user.getWork_class());
				
				BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
				LocalDateTime currentDateTime = LocalDateTime.now();
				Date dateValue = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant());
				audit.setAudit_date(new Date());
				audit.setEntry_time(dateValue);
				audit.setEntry_user(user.getUserid());
				audit.setFunc_code("LOGIN");
				audit.setRemarks("Login Successfully");
				audit.setAudit_table("BRECON_USER_PROFILE_TABLE");
				audit.setAudit_screen("LOGIN");
				audit.setEvent_id(user.getUserid());
				audit.setEvent_name(user.getUsername());
				audit.setModi_details("-");
				USER_PROFILE_ENTITY auth_user = userProfileRep.getRole(user.getUserid());
				String auth_user_val = auth_user.getAuth_user();
				Date auth_user_date = auth_user.getAuth_time();
				audit.setAuth_user(auth_user_val);
				audit.setAuth_time(auth_user_date);
				audit.setAudit_ref_no(auditID.toString());
				AuditRepo.save(audit);
				
				response.sendRedirect("Dashboard");
			}

		};

	}
	
	@Bean
	public LogoutSuccessHandler xbrlLogoutSuccessHandler() {
		
		return new LogoutSuccessHandler() {

			@Override
			public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				Optional<USER_PROFILE_ENTITY> up = userProfileRep.findById(authentication.getName());
				USER_PROFILE_ENTITY user = up.get();
				BRECON_Audit_Entity audit = new BRECON_Audit_Entity();
				String Number1 = sequence.generateRequestUUId();
				audit.setAudit_date(new Date());
				audit.setEntry_time(new Date());
				audit.setEntry_user(user.getUserid());
				audit.setFunc_code("LOGOUT");
				audit.setRemarks("Logout Successfully");
				audit.setAudit_table("BRECON_USER_PROFILE_TABLE");
				audit.setAudit_screen("LOGOUT");
				audit.setEvent_id(user.getUserid());
				audit.setEvent_name(user.getUsername());
				USER_PROFILE_ENTITY auth_user = userProfileRep.getRole(user.getUserid());
				String auth_user_val = auth_user.getAuth_user();
				Date auth_user_date = auth_user.getAuth_time();
				audit.setAuth_user(auth_user_val);
				audit.setAuth_time(auth_user_date);
				audit.setModi_details("-");
				audit.setAudit_ref_no(Number1.toString());
				AuditRepo.save(audit);
				response.sendRedirect("login?logout");
			
			}
		
		
	};
	
}
	
	
}