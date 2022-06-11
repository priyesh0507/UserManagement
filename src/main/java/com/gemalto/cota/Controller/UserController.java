package com.gemalto.cota.Controller;

import java.util.List;

import com.gemalto.cota.entities.User;
import com.gemalto.cota.error.UserNotFoundException;
import com.gemalto.cota.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserRepository repository;

    // Find
    @GetMapping("/users")
    List<User> findAll() {
        return repository.findAll();
    }

    // Save
    @PostMapping("/users")
    //return 201 instead of 200
    @ResponseStatus(HttpStatus.CREATED)
    User newUser(
            @RequestBody
                    User newUser) {
        return repository.save(newUser);
    }

    // Find
    @GetMapping("/users/{id}")
    User findOne(
            @PathVariable
                    Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    // Save or update
    @PutMapping("/users/{id}")
    User saveOrUpdate(
            @RequestBody
                    User newUser,
            @PathVariable
                    Long id) {

        return repository.findById(id)
                .map(x -> {
                    x.setName(newUser.getName());
                    x.setPassword(newUser.getPassword());
                    x.setStatus(newUser.isStatus());
                    return repository.save(x);
                })
                .orElseGet(() -> {
                    newUser.setId(id);
                    return repository.save(newUser);
                });
    }





  /*  // update author only
    @PatchMapping("/users/{id}")
    User patch(@RequestBody Map<String, String> update, @PathVariable Long id) {

        return repository.findById(id)
                .map(x -> {

                    String author = update.get("author");
                    if (!StringUtils.isEmpty(author)) {
                        x.setAuthor(author);

                        // better create a custom method to update a value = :newValue where id = :id
                        return repository.save(x);
                    } else {
                        throw new UserUnSupportedFieldPatchException(update.keySet());
                    }

                })
                .orElseGet(() -> {
                    throw new UserNotFoundException(id);
                });

    }*/

    @DeleteMapping("/users/{id}")
    void deleteUser(
            @PathVariable
                    Long id) {
        repository.deleteById(id);
    }

}
