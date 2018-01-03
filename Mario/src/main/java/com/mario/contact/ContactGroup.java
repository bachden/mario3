package com.mario.contact;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactGroup {

	private String name;
	private final Set<String> contactNames = new HashSet<>();
}
