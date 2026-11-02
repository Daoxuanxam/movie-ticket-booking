package com.cinema.booking;

import com.cinema.booking.model.User;
import com.cinema.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class LoginRedirectTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    public void testAdminLoginRedirectsToDashboard() throws Exception {
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("password");
        admin.setRole(User.Role.admin);
        admin.setIsActive(true);

        Mockito.when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        mockMvc.perform(post("/login")
                        .param("email", "admin@test.com")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    public void testCustomerLoginRedirectsToHome() throws Exception {
        User customer = new User();
        customer.setEmail("customer@test.com");
        customer.setPasswordHash("password");
        customer.setRole(User.Role.customer);
        customer.setIsActive(true);

        Mockito.when(userRepository.findByEmail("customer@test.com")).thenReturn(Optional.of(customer));

        mockMvc.perform(post("/login")
                        .param("email", "customer@test.com")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testAdminAccessingHomeRedirectedToDashboard() throws Exception {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@test.com", "password",
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN"))
        );

        mockMvc.perform(get("/")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/dashboard"));
    }

    @Test
    public void testCustomerAccessingHomeNotRedirected() throws Exception {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(
                new UsernamePasswordAuthenticationToken("customer@test.com", "password",
                        AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"))
        );

        mockMvc.perform(get("/")
                        .sessionAttr(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext))
                .andExpect(status().isOk());
    }

    @Test
    public void testLoginWithNonExistentEmail() throws Exception {
        Mockito.when(userRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", "nonexistent@test.com")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        // First GET request: should have error in model
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Email không tồn tại trong hệ thống."));

        // Second GET request: error should be cleared (only shown once!)
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    public void testLoginWithWrongPassword() throws Exception {
        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPasswordHash("password");
        admin.setRole(User.Role.admin);
        admin.setIsActive(true);

        Mockito.when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));

        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", "admin@test.com")
                        .param("password", "wrongpassword"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        // First GET request: should have error in model
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Mật khẩu không chính xác."));

        // Second GET request: error should be cleared
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    public void testLoginWithDisabledAccount() throws Exception {
        User disabled = new User();
        disabled.setEmail("disabled@test.com");
        disabled.setPasswordHash("password");
        disabled.setRole(User.Role.customer);
        disabled.setIsActive(false);

        Mockito.when(userRepository.findByEmail("disabled@test.com")).thenReturn(Optional.of(disabled));

        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", "disabled@test.com")
                        .param("password", "password"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession();

        // First GET request: should have error in model
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "Tài khoản đã bị khóa."));

        // Second GET request: error should be cleared
        mockMvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"));
    }
}
