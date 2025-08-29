package com.javarush.jira.profile.internal.web;

import com.javarush.jira.common.error.ErrorMessageHandler;
import com.javarush.jira.login.AuthUser;
import com.javarush.jira.login.Role;
import com.javarush.jira.login.User;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;


import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfileRestController.class)
class ProfileRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileMapper profileMapper;

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @MockBean
    private ErrorMessageHandler errorMessageHandler;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ProfileRepository profileRepository;

    private SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor authRequest;

    private AuthUser authUser;
    private ProfileTo profileTo;

    @BeforeEach
    void setUp() {
        User user = new User(
                1L,
                "testuser@example.com",
                "password",
                "Test",
                "User",
                "testuser",
                Role.DEV
        );
        authUser = new AuthUser(user);

        profileTo = new ProfileTo(1L, Set.of("MAIL_1"), Set.of());
    }



    @Test
    void getProfile_success() throws Exception {
        Profile profileMock = Mockito.mock(Profile.class);
        when(profileRepository.getOrCreate(authUser.id())).thenReturn(profileMock);
        when(profileMapper.toTo(any())).thenReturn(profileTo);

        mockMvc.perform(get(ProfileRestController.REST_URL)
                        .with(user(authUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mailNotifications[0]").value("MAIL_1"));

        verify(profileRepository).getOrCreate(authUser.id());
        verify(profileMapper).toTo(any());
    }


    @Test
    @WithMockUser
    void getProfile_repositoryThrows_exception() throws Exception {
        when(profileRepository.getOrCreate(authUser.id())).thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(get(ProfileRestController.REST_URL)
                        .principal(new UsernamePasswordAuthenticationToken(authUser, authUser.getPassword(), authUser.getAuthorities())))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void updateProfile_success() throws Exception {
        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setEmail("user@example.com");
        userEntity.setPassword("password");
        userEntity.setFirstName("User");
        userEntity.setLastName("Test");
        userEntity.setDisplayName("User Test");
        userEntity.setRoles(Collections.singleton(Role.DEV));

        AuthUser authUser = new AuthUser(userEntity);

        mockMvc.perform(put("/api/profile")
                        .with(user(authUser))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 1,
                              "mailNotifications": ["MAIL_1"],
                              "contacts": []
                            }
                            """))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void updateProfile_validationError() throws Exception {
        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .principal(new UsernamePasswordAuthenticationToken(authUser, authUser.getPassword(), authUser.getAuthorities()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 1,
                              "mailNotifications": [""],
                              "contacts": []
                            }
                            """)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    void updateProfile_repositoryThrows_exception() throws Exception {
        when(profileRepository.getOrCreate(profileTo.getId()))
                .thenThrow(new RuntimeException("DB error"));

        mockMvc.perform(put(ProfileRestController.REST_URL)
                        .principal(new UsernamePasswordAuthenticationToken(
                                authUser, authUser.getPassword(), authUser.getAuthorities()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "id": 1,
                              "mailNotifications": ["MAIL_1"],
                              "contacts": []
                            }
                            """)
                        .with(csrf()) //
                )
                .andExpect(status().isInternalServerError());
    }


}
