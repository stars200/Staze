/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.features.impl.dungeons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import skytils.skytilsmod.Skytils;
import skytils.skytilsmod.core.structure.FloatPair;
import skytils.skytilsmod.core.structure.GuiElement;
import skytils.skytilsmod.utils.StringUtils;
import skytils.skytilsmod.utils.Utils;
import skytils.skytilsmod.utils.graphics.ScreenRenderer;
import skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import skytils.skytilsmod.utils.graphics.colors.CommonColors;

import java.util.List;
import java.util.Objects;

public class BossHPDisplays {

    private static final Minecraft mc = Minecraft.getMinecraft();


    private static boolean canGiantsSpawn = false;

    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
    public void onChat(ClientChatReceivedEvent event) {
        if (!Utils.inDungeons || event.type == 2) return;
        String unformatted = StringUtils.stripControlCodes(event.message.getUnformattedText());
        if (unformatted.startsWith("[BOSS] Sadan")) {
            if (unformatted.contains("My giants! Unleashed!"))
                canGiantsSpawn = true;
            if (unformatted.contains("It was inevitable.") || unformatted.contains("NOOOOOOOOO"))
                canGiantsSpawn = false;
        }
        if (unformatted.equals("[BOSS] The Watcher: Plus I needed to give my new friends some space to roam..."))
            canGiantsSpawn = true;
        if (unformatted.startsWith("[BOSS] The Watcher: You have failed to prove yourself, and have paid with your lives.") || unformatted.startsWith("[BOSS] The Watcher: You have proven yourself"))
           canGiantsSpawn = false;
    }

    @SubscribeEvent
    public void onWorldChange(WorldEvent.Load event) {
        canGiantsSpawn = false;
    }

    static {
        new GiantHPElement();
    }

    public static class GiantHPElement extends GuiElement {

        private static final String[] GIANT_NAMES = {"§3§lThe Diamond Giant", "§c§lBigfoot", "§4§lL.A.S.R.", "§d§lJolly Pink Giant" };

        public GiantHPElement() {
            super("Show Giant HP", new FloatPair(200, 30));
            Skytils.GUIMANAGER.registerElement(this);
        }

        @Override
        public void render() {
            EntityPlayerSP player = mc.thePlayer;
            World world = mc.theWorld;
            if (canGiantsSpawn && this.getToggled() && Utils.inSkyblock && player != null && world != null) {
                float x = this.getActualX();
                float y = this.getActualY();

                List<EntityArmorStand> giantNames = world.getEntities(EntityArmorStand.class, (entity) -> {
                    String name = entity.getDisplayName().getFormattedText();
                    if (name.contains("❤")) {
                        if (name.contains("§e﴾ §c§lSadan§r")) {
                            return true;
                        } else if (name.contains("Giant") && Objects.equals(DungeonsFeatures.dungeonFloor, "F7")) return true;

                        for (String giant : GIANT_NAMES) {
                            if (name.contains(giant)) return true;
                        }
                    }
                    return false;
                });

                giantNames.removeIf(entity -> entity.getDisplayName().getFormattedText().contains("Sadan") && giantNames.size() > 1);

                ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

                boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

                for (int i = 0; i < giantNames.size(); i++) {
                    SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                    ScreenRenderer.fontRenderer.drawString(giantNames.get(i).getDisplayName().getFormattedText(), leftAlign ? 0 : getActualWidth(), i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
                }
            }
        }

        @Override
        public void demoRender() {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            boolean leftAlign = getActualX() < sr.getScaledWidth() / 2f;

            for (int i = 0; i < GIANT_NAMES.length; i++) {
                String text = GIANT_NAMES[i] + " §a20M§c❤";
                SmartFontRenderer.TextAlignment alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                ScreenRenderer.fontRenderer.drawString(text, leftAlign ? 0 : getActualWidth(), i * ScreenRenderer.fontRenderer.FONT_HEIGHT, CommonColors.WHITE, alignment, SmartFontRenderer.TextShadow.NORMAL);
            }
        }

        @Override
        public int getHeight() {
            return ScreenRenderer.fontRenderer.FONT_HEIGHT * 4;
        }

        @Override
        public int getWidth() {
            return ScreenRenderer.fontRenderer.getStringWidth("§3§lThe Diamond Giant §a19.9M§c❤");
        }

        @Override
        public boolean getToggled() {
            return Skytils.config.showGiantHP;
        }
    }
}
