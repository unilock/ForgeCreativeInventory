package cc.unilock.forgecreativeinv;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;
import java.util.Objects;

public class FCIMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return Objects.equals(mixinClassName, "net.fabricmc.fabric.mixin.item.group.client.CreativeInventoryScreenMixin");
    }
}
