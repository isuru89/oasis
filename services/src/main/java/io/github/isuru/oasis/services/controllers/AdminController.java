package io.github.isuru.oasis.services.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/admin")
public class AdminController {

    @PostMapping("/user/add")
    @ResponseBody
    public void addUser() {

    }

    @PostMapping("/user/{id}/edit")
    @ResponseBody
    public void editUser(@PathVariable("id") long userId) {

    }

    @GetMapping("/user/{id}")
    @ResponseBody
    public void readUser(@PathVariable("id") long userId) {

    }

    @GetMapping("/user/ext/{id}")
    @ResponseBody
    public void readUserByExternalId(@PathVariable("id") String externalId) {

    }

    @DeleteMapping("/user/{id}")
    @ResponseBody
    public void deleteUser(@PathVariable("id") long userId) {

    }

    @PostMapping("/team/add")
    @ResponseBody
    public void addTeam() {

    }

    @PostMapping("/team/{id}/edit")
    @ResponseBody
    public void editTeam(@PathVariable("id") int teamId) {

    }

    @GetMapping("/team/{id}")
    @ResponseBody
    public void readTeam(@PathVariable("id") int teamId) {

    }

    @PostMapping("/team/{id}/users")
    @ResponseBody
    public void getUsersOfTeam(@PathVariable("id") int teamId) {

    }


    @PostMapping("/scope/add")
    @ResponseBody
    public void addTeamScope() {

    }

    @PostMapping("/scope/{id}/edit")
    @ResponseBody
    public void editTeamScope(@PathVariable("id") int scopeId) {

    }

    @GetMapping("/scope/list")
    @ResponseBody
    public void readAllTeamScopes() {

    }

    @GetMapping("/scope/{id}")
    @ResponseBody
    public void readTeamScope(@PathVariable("id") int scopeId) {

    }

    @PostMapping("/scope/{id}/teams")
    @ResponseBody
    public void readTeamsInTeamScope(@PathVariable("id") int scopeId) {

    }


    @PostMapping("/user/add-to-team")
    @ResponseBody
    public void addUserToTeam() {

    }

    @PostMapping("/user/{id}/current-team")
    @ResponseBody
    public void findTeamOfUser(@PathVariable("id") long userId) {

    }

}
