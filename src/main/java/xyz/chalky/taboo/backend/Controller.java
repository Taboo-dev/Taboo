package xyz.chalky.taboo.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.chalky.taboo.database.model.Config;
import xyz.chalky.taboo.database.repository.ConfigRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class Controller {

    @Autowired private ConfigRepository configRepository;

    @GetMapping("/config/{id}")
    public ResponseEntity<Config> getConfigById(@PathVariable("id") long id) {
        Optional<Config> optConfig = configRepository.findById(id);
        return optConfig.map(config -> new ResponseEntity<>(config, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/config/{id}")
    public ResponseEntity<Config> setConfig(@PathVariable("id") long guildId, @RequestParam("logId") long logId, @RequestParam("musicId") long musicId) {
        Config config = new Config(guildId, logId, musicId);
        try {
            configRepository.save(config);
            return new ResponseEntity<>(config, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(config, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
