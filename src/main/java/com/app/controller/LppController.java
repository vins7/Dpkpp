package com.app.controller;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.app.service.LppService;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/lpp")
public class LppController {

	@Autowired
	private LppService lppService;
	
	@PostMapping
	@Transactional
	public ResponseEntity<?> add(MultipartFile file,String lpp) throws Exception {
		try {
			lppService.add(file,lpp);
			return new ResponseEntity<>("Success", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}
	
//	@PostMapping
//	@Transactional
//	@PreAuthorize("hasAuthority('ROLE_VERIFICATOR')")
//	public ResponseEntity<?> addVerification(@RequestParam String id) throws Exception {
//		try {
//			lppService.addVerification(id);
//			return new ResponseEntity<>("Success", HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//		}
//	}
	
//	@GetMapping(value = "/get")
//	public ResponseEntity<?> getListLpp() throws Exception {
//		try {			
//			return new ResponseEntity<>(lppService.getListLpp(), HttpStatus.OK);
//		} catch (Exception e) {
//			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//		}
//	}
}