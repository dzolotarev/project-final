package com.javarush.jira.profile.internal.web;

import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileRepository;
import com.javarush.jira.profile.internal.model.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.javarush.jira.common.util.JsonUtil.readValue;
import static com.javarush.jira.common.util.JsonUtil.writeValue;
import static com.javarush.jira.login.internal.web.UserTestData.*;
import static com.javarush.jira.profile.internal.web.ProfileRestController.REST_URL;
import static com.javarush.jira.profile.internal.web.ProfileTestData.getUpdated;
import static com.javarush.jira.profile.internal.web.ProfileTestData.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileRestControllerTest extends AbstractControllerTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Test
    @WithUserDetails(value = USER_MAIL)
    void get_WhenAuthorizedAsUser_returnValidProfileTo() throws Exception {

        MvcResult result = perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        String jsonContent = result.getResponse().getContentAsString();
        ProfileTo profileToFromResponse = readValue(jsonContent, ProfileTo.class);
        assertEquals(USER_PROFILE_TO, profileToFromResponse);
    }

    @Test
    @WithUserDetails(value = GUEST_MAIL)
    void get_WhenAuthorizedAsGuest_returnValidEmptyProfileTo() throws Exception {

        MvcResult result = perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();
        String jsonContent = result.getResponse().getContentAsString();
        ProfileTo profileToFromResponse = readValue(jsonContent, ProfileTo.class);
        assertEquals(GUEST_PROFILE_EMPTY_TO, profileToFromResponse);

    }

    @Test
    void getUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(REST_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update() throws Exception {

        ProfileTo updatedTo = getUpdatedTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(updatedTo)))
                .andDo(print())
                .andExpect(status().isNoContent());

        Profile updated = getUpdated(USER_ID);
        PROFILE_MATCHER.assertMatch(profileRepository.getOrCreate(USER_ID), updated);

    }

    @Test
    void updateUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getUpdatedTo())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update_WhenWrongID_thenIllegalRequestDataExceptionThrown() throws Exception {
        ProfileTo profileTo = getNewTo();
        profileTo.setId(GUEST_ID);
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update_WithUnknownContactTo_thenIllegalArgumentExceptionThrown() throws Exception {
        ProfileTo profileTo = getWithUnknownContactTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update_WithUnknownNotificationTo_thenIllegalArgumentExceptionThrown() throws Exception {
        ProfileTo profileTo = getWithUnknownNotificationTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update_WithInvalidProfileTo() throws Exception {
        ProfileTo profileTo = getInvalidTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithUserDetails(value = USER_MAIL)
    void update_WithContactHtmlUnsafeTo() throws Exception {
        ProfileTo profileTo = getWithContactHtmlUnsafeTo();
        perform(MockMvcRequestBuilders.put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(profileTo)))
                .andExpect(status().isUnprocessableEntity());
    }
}