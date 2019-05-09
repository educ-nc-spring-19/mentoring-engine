package com.educ_nc_spring_19.mentoring_engine.service;

import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.MentorDTO;
import com.educ_nc_spring_19.educ_nc_spring_19_common.common.dto.StudentDTO;
import com.educ_nc_spring_19.mentoring_engine.client.MasterDataClient;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Cauldron;
import com.educ_nc_spring_19.mentoring_engine.model.entity.Pool;
import com.educ_nc_spring_19.mentoring_engine.service.repo.CauldronRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.IterableUtils;
import org.apache.logging.log4j.Level;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
@Service
public class CauldronService {
    private final CauldronRepository cauldronRepository;
    private final MasterDataClient masterDataClient;
    private final PoolService poolService;
    private final UserService userService;

    public void deleteAll() {
        cauldronRepository.deleteAll();
    }

    public List<Cauldron> findAll() {
        List<Cauldron> cauldrons = IterableUtils.toList(cauldronRepository.findAll());

        log.log(Level.DEBUG,
                "Cauldrons found by findAll(): " + cauldrons.stream().map(Cauldron::getId).collect(Collectors.toList()));
        return cauldrons;
    }

    public List<Cauldron> findAllById(Iterable<UUID> ids) {
        List<Cauldron> cauldrons = IterableUtils.toList(cauldronRepository.findAllById(ids));

        log.log(Level.DEBUG,
                "Cauldrons found by findAllById(): "
                        + cauldrons.stream().map(Cauldron::getId).collect(Collectors.toList())
        );
        return cauldrons;
    }

    public Optional<Cauldron> findById(UUID id) {
        Optional<Cauldron> optionalCauldron = cauldronRepository.findById(id);
        optionalCauldron.ifPresent(cauldron ->
                log.log(Level.DEBUG, "Pool(id=" + cauldron.getId() + ") found by id"));
        return optionalCauldron;
    }

    public Optional<Cauldron> findByMentorsIsAndStudentsIs(UUID mentorId, UUID studentId) {
        return cauldronRepository.findByMentorsIsAndStudentsIs(mentorId, studentId);
    }

    public Cauldron removeStudentId(UUID studentId) throws IllegalArgumentException, NoSuchElementException {
        // get current mentor
        UUID currentUserId = userService.getCurrentUserId();
        MentorDTO currentMentorDTO = masterDataClient.getMentorByUserId(currentUserId);

        if (currentMentorDTO == null) {
            log.log(Level.WARN, "Can't find Mentor by User(id=" + currentUserId.toString() + ")");
            throw new NoSuchElementException("Can't find Mentor by User(id=" + currentUserId.toString() + ")");
        }

        Optional<Cauldron> optionalCauldron = cauldronRepository.findByMentorsIs(currentMentorDTO.getId());
        if (optionalCauldron.isPresent()) {
            Cauldron cauldron = optionalCauldron.get();
            Set<UUID> studentIds = cauldron.getStudents();
            if (!studentIds.contains(studentId)) {
                throw new IllegalArgumentException("Student(id=" + studentId
                        + ") is absent in Cauldron(id=" + cauldron.getId() + ")");
            }

            StudentDTO studentDTO = masterDataClient.getStudentById(studentId);
            if (studentDTO == null) {
                throw new IllegalArgumentException("Can't get Student(id=" + studentId + ") from master-data service");
            } else if (studentDTO.getDirectionId() == null) {
                throw new NoSuchElementException("Student(id=" + studentId + ") direction is null");
            }

            Optional<Pool> optionalPool = poolService.findByDirectionId(studentDTO.getDirectionId());
            if (!optionalPool.isPresent()) {
                throw new NoSuchElementException("There is no Pool for Direction(id=" + studentDTO.getDirectionId() + ")");
            }

            // remove student from cauldron
            cauldron.getStudents().remove(studentId);

            // add student to pool
            Pool pool = optionalPool.get();
            pool.getStudents().add(studentId);

            // save data
            cauldron = cauldronRepository.save(cauldron);
            pool = poolService.save(pool);

            log.log(Level.INFO, "Student(id=" + studentId
                    + ") removed from Cauldron(id=" + cauldron.getId()
                    + ") and added to Pool(id=" + pool.getId() + ")");

            return cauldron;
        } else {
            throw new IllegalArgumentException("Cauldron for Mentor(id=" + currentMentorDTO.getId() + ") doesn't exist");
        }
    }

    public Cauldron save(Cauldron cauldron) {
        return cauldronRepository.save(cauldron);
    }

    public List<Cauldron> saveAll(Iterable<Cauldron> cauldrons) {
        return IterableUtils.toList(cauldronRepository.saveAll(cauldrons));
    }
}
