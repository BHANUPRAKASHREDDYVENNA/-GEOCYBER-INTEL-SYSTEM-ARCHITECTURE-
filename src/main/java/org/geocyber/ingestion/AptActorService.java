package org.geocyber.ingestion;

import org.geocyber.domain.AptActor;
import org.geocyber.repository.AptActorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AptActorService {

    private static final Logger log = LoggerFactory.getLogger(AptActorService.class);

    private final AptActorRepository actorRepository;

    // Fast in-memory alias index: Alias (lowercase) -> Canonical Name
    private final Map<String, String> aliasMap = new HashMap<>();

    public AptActorService(AptActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    /**
     * Resolves any known vendor alias or code name to the canonical threat actor name.
     * e.g. "STRONTIUM" -> "APT28", "Midnight Blizzard" -> "APT29", "Labyrinth Chollima" -> "Lazarus Group"
     */
    public String resolveAlias(String rawName) {
        if (rawName == null || rawName.isBlank()) return "UNKNOWN";
        String cleaned = rawName.trim().toLowerCase();

        if (aliasMap.isEmpty()) {
            rebuildAliasIndex();
        }

        String matched = aliasMap.get(cleaned);
        if (matched != null) {
            return matched;
        }

        // Check if any alias key is contained in rawName
        for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
            if (cleaned.contains(entry.getKey()) || entry.getKey().contains(cleaned)) {
                return entry.getValue();
            }
        }

        return rawName.trim();
    }

    /**
     * Rebuild alias lookup table from database
     */
    public synchronized void rebuildAliasIndex() {
        aliasMap.clear();
        List<AptActor> actors = actorRepository.findAll();
        for (AptActor actor : actors) {
            aliasMap.put(actor.getName().toLowerCase(), actor.getName());
            if (actor.getAliases() != null && !actor.getAliases().isBlank()) {
                String[] aliases = actor.getAliases().split("[,;|]");
                for (String alias : aliases) {
                    if (!alias.isBlank()) {
                        aliasMap.put(alias.trim().toLowerCase(), actor.getName());
                    }
                }
            }
        }
    }

    public List<AptActor> getAllActors() {
        return actorRepository.findAll();
    }

    public Optional<AptActor> getActorByName(String name) {
        String canonical = resolveAlias(name);
        return actorRepository.findByNameIgnoreCase(canonical);
    }

    @Transactional
    public AptActor saveActor(AptActor actor) {
        AptActor saved = actorRepository.save(actor);
        rebuildAliasIndex();
        return saved;
    }
}
