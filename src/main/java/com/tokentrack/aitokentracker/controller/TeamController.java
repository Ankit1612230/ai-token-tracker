package com.tokentrack.aitokentracker.controller;

import com.tokentrack.aitokentracker.dto.CreateTeamRequest;
import com.tokentrack.aitokentracker.entity.Company;
import com.tokentrack.aitokentracker.entity.Team;
import com.tokentrack.aitokentracker.repository.CompanyRepository;
import com.tokentrack.aitokentracker.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/companies/{companyId}/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @PostMapping
    public Team createTeam(
            @PathVariable UUID companyId,
            @RequestBody CreateTeamRequest request
    ) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Team team = new Team();
        team.setCompany(company);
        team.setName(request.getName());

        return teamRepository.save(team);
    }

    @GetMapping
    public List<Team> listTeams(@PathVariable UUID companyId) {
        return teamRepository.findByCompanyId(companyId);
    }
}