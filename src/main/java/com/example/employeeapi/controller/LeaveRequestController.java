package com.example.employeeapi.controller;

import com.example.employeeapi.entity.LeaveRequest;
import com.example.employeeapi.enums.LeaveRequestStatus;
import com.example.employeeapi.model.LeaveRequestDto;
import com.example.employeeapi.service.LeaveRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaveRequest")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Autowired
    public LeaveRequestController(LeaveRequestService leaveRequestService) {
        this.leaveRequestService = leaveRequestService;
    }

    @PostMapping
    public ResponseEntity createLeaveRequest(@RequestBody LeaveRequestDto leaveRequestDto) {
        try{
        LeaveRequest leaveRequest = leaveRequestService.createLeaveRequest(leaveRequestDto);
        return new ResponseEntity<>(leaveRequest, HttpStatus.CREATED);
        }catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<List<LeaveRequest>> listLeaveRequests() {
        List<LeaveRequest> leaveRequestList = leaveRequestService.list();
        return new ResponseEntity<>(leaveRequestList, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeaveRequest> updateLeaveRequest(@PathVariable Long id, @RequestBody LeaveRequestDto leaveRequest) {
        LeaveRequest updatedLeaveRequest = leaveRequestService.updateLeaveRequest(id, leaveRequest);
        return new ResponseEntity<>(updatedLeaveRequest, HttpStatus.OK);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity updateLeaveRequestStatus(@PathVariable Long id, @RequestBody LeaveRequestStatus status) {
        try {
            LeaveRequest updatedLeaveRequest = leaveRequestService.updateLeaveRequestStatus(id, status);
            return new ResponseEntity<>(updatedLeaveRequest, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLeaveRequest(@PathVariable Long id) {
        leaveRequestService.deleteLeaveRequest(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}


