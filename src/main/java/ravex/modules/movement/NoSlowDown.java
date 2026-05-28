package ravex.modules.movement;

import ravex.modules.Category;
import ravex.modules.Module;
import ravex.parameter.ModeParameter;
import java.util.List;

public class NoSlowDown extends Module {
    public static final NoSlowDown INSTANCE = new NoSlowDown();

    public final ModeParameter mode = new ModeParameter("Mode", "Vanilla",
            List.of("Vanilla", "Grim", "NCP"));
    public final ravex.parameter.BooleanParameter items = new ravex.parameter.BooleanParameter("Items", true);
    public final ravex.parameter.BooleanParameter blocks = new ravex.parameter.BooleanParameter("Blocks", true);
    public final ravex.parameter.BooleanParameter cobwebs = new ravex.parameter.BooleanParameter("Cobwebs", true);

    private NoSlowDown() {
        super("NoSlowDown", Category.MOVEMENT);
        addParameter(mode);
        addParameter(items);
        addParameter(blocks);
        addParameter(cobwebs);
    }
}
