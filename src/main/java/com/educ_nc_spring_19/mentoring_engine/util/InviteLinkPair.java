package com.educ_nc_spring_19.mentoring_engine.util;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URL;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class InviteLinkPair {
    URL acceptLink;
    URL declineLink;
}
