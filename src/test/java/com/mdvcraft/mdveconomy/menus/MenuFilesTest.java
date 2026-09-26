package com.mdvcraft.mdveconomy.menus;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class MenuFilesTest {
    static Stream<String> names(){return MenuFiles.NAMES.stream();}
    static YamlConfiguration read(String name) throws Exception {
        var c=new YamlConfiguration();try(var input=MenuFilesTest.class.getResourceAsStream("/menus/"+name+".yml")) {
            assertNotNull(input);c.load(new InputStreamReader(input,StandardCharsets.UTF_8));}return c;
    }
    @ParameterizedTest @MethodSource("names") void allLayoutsHaveValidNonOverlappingSlots(String name)throws Exception {MenuFiles.validate(name,read(name));}
    @Test void duplicateButtonSlotIsRejected()throws Exception {var c=read("main");c.set("buttons.close.slot",20);assertThrows(IllegalArgumentException.class,()->MenuFiles.validate("main",c));}
    @Test void creationMustHaveOneInput()throws Exception {var c=read("create");c.set("input-slots",java.util.List.of());assertThrows(IllegalArgumentException.class,()->MenuFiles.validate("create",c));}
    @Test void tradeMustHaveFiveInputs()throws Exception {var c=read("trade");c.set("input-slots",java.util.List.of(20));assertThrows(IllegalArgumentException.class,()->MenuFiles.validate("trade",c));}
    @Test void invalidTextureRejected() {assertThrows(IllegalArgumentException.class,()->IconFactory.textureUrl("invalid"));}
    @Test void missingFillerRejectedBeforeOpening()throws Exception {var c=read("main");c.set("filler",null);assertThrows(IllegalArgumentException.class,()->MenuFiles.validate("main",c));}
    @Test void invalidActionRejectedOnLoad()throws Exception {var c=read("main");c.set("buttons.close.action","TYPO");assertThrows(IllegalArgumentException.class,()->MenuFiles.validate("main",c));}
    @Test void overlappingDecorationRejected()throws Exception {var c=read("create");c.set("decorations",java.util.List.of(java.util.Map.of("material","STONE","slots",java.util.List.of(13))));assertThrows(IllegalArgumentException.class,()->MenuFiles.validate("create",c));}
    @Test void validBase64TextureAndNonMinecraftHostRejected() {
        String valid="{\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/abc123\"}}}";
        assertEquals("textures.minecraft.net",IconFactory.textureUrl(java.util.Base64.getEncoder().encodeToString(valid.getBytes(StandardCharsets.UTF_8))).getHost());
        String invalid=valid.replace("textures.minecraft.net","example.com");
        assertThrows(IllegalArgumentException.class,()->IconFactory.textureUrl(java.util.Base64.getEncoder().encodeToString(invalid.getBytes(StandardCharsets.UTF_8))));
    }
}
