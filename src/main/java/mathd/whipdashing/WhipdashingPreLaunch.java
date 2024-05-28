package mathd.whipdashing;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

import static com.llamalad7.mixinextras.MixinExtrasBootstrap.*;

public class WhipdashingPreLaunch implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        init();
    }
}