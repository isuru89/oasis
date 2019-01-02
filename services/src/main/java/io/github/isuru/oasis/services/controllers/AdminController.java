package io.github.isuru.oasis.services.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    @PostMapping("/admin/user/add")
    @ResponseBody
    public void addUser() {

    }

    @PostMapping("/admin/user/{id}/edit")
    @ResponseBody
    public void editUser(@PathVariable("id") long userId) {

    }

    @GetMapping("/admin/user/{id}")
    @ResponseBody
    public void readUser(@PathVariable("id") long userId) {

    }

    @GetMapping("/admin/user/ext/{id}")
    @ResponseBody
    public void readUserByExternalId(@PathVariable("id") String externalId) {

    }

    @DeleteMapping("/admin/user/{id}")
    @ResponseBody
    public void deleteUser(@PathVariable("id") long userId) {

    }

    @PostMapping("/admin/team/add")
    @ResponseBody
    public void addTeam() {

    }

    @PostMapping("/admin/team/{id}/edit")
    @ResponseBody
    public void editTeam(@PathVariable("id") int teamId) {

    }

    @GetMapping("/admin/team/{id}")
    @ResponseBody
    public void readTeam(@PathVariable("id") int teamId) {

    }

    @PostMapping("/admin/team/{id}/users")
    @ResponseBody
    public void getUsersOfTeam(@PathVariable("id") int teamId) {

    }


    @PostMapping("/admin/scope/add")
    @ResponseBody
    public void addTeamScope() {

    }

    @PostMapping("/admin/scope/{id}/edit")
    @ResponseBody
    public void editTeamScope(@PathVariable("id") int scopeId) {

    }

    @GetMapping("/admin/scope/list")
    @ResponseBody
    public void readAllTeamScopes() {

    }

    @GetMapping("/admin/scope/{id}")
    @ResponseBody
    public void readTeamScope(@PathVariable("id") int scopeId) {

    }

    @PostMapping("/admin/scope/{id}/teams")
    @ResponseBody
    public void readTeamsInTeamScope(@PathVariable("id") int scopeId) {

    }


    @PostMapping("/admin/user/add-to-team")
    @ResponseBody
    public void addUserToTeam() {

    }

    @PostMapping("/admin/user/{id}/current-team")
    @ResponseBody
    public void findTeamOfUser(@PathVariable("id") long userId) {

    }

    @PostMapping("/admin/user/{id}/change-hero/{heroId}")
    @ResponseBody
    public void changeUserHero(@PathVariable("id") long userId, @PathVariable("heroId") int heroId) {

    }
}
