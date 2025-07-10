package net.picopress.mc.mods.zombietactics2.ai.path.navigation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.level.Level;


/*
 * allows setting cnaFloat
 * superclass has setCanFloat method that does nothing
 */
public class AmphibiousNavigation extends AmphibiousPathNavigation {
    public AmphibiousNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    public void setCanFloat(boolean canFloat) {
        super.nodeEvaluator.setCanFloat(canFloat);
    }
}
