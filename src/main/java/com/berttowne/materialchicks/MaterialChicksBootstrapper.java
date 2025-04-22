package com.berttowne.materialchicks;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.event.RegistryEvents;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Chicken;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class MaterialChicksBootstrapper implements PluginBootstrap {

    @Override
    @SuppressWarnings("PatternValidation")
    public void bootstrap(@NotNull BootstrapContext context) {
        try {
            String pathString = "/pack/assets/materialchicks/models/item";
            URI uri = Objects.requireNonNull(MaterialChicks.class.getResource(pathString)).toURI();

            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                // This is to ensure the file system is created and available
                Path chickensFolder = fs.getPath(pathString);

                try (Stream<Path> walk = Files.walk(chickensFolder, 1)) {
                    walk.forEach(filePath -> {
                        String chickenName = filePath.getFileName().toString().replace(".json", "");

                        context.getLifecycleManager().registerEventHandler(RegistryEvents.CHICKEN_VARIANT.freeze(), event ->
                            event.registry().register(TypedKey.create(RegistryKey.CHICKEN_VARIANT, Key.key("materialchicks", chickenName)),
                                builder -> builder.model(Chicken.Variant.Model.NORMAL).assetId(Key.key("materialchicks:entity/chicken/" + chickenName))));
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (FileSystemAlreadyExistsException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}