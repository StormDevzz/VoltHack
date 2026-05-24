package volthack;

import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.Camera;

public class GLTest {
    public static void test(Camera camera) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        
        builder.addVertex(0f, 0f, 0f).setColor(255, 255, 255, 255);
        
        RenderTypes.lines().draw(builder.build());
    }
}
