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
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class MaterialChicksBootstrapper implements PluginBootstrap {

    @Override
    @SuppressWarnings("PatternValidation")
    public void bootstrap(@NotNull BootstrapContext context) {
        Path chickensFolder;
        try {
            chickensFolder = Paths.get(Objects.requireNonNull(MaterialChicks.class.getClassLoader().getResource("pack/assets/materialchicks/models/item")).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(chickensFolder, "*.json")) {
            for (Path filePath : stream) {
                String chickenName = filePath.getFileName().toString().replace(".json", "");

                context.getLifecycleManager().registerEventHandler(RegistryEvents.CHICKEN_VARIANT.freeze(), event ->
                        event.registry().register(TypedKey.create(RegistryKey.CHICKEN_VARIANT, Key.key("materialchicks", chickenName)),
                                builder -> builder.model(Chicken.Variant.Model.NORMAL).assetId(Key.key("materialchicks:entity/chicken/" + chickenName))));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}