package com.game.alkoninja.swipe.mesh;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.game.alkoninja.swipe.SwipeHandler;

public class SwipeTriStrip {

    Array<Vector2> texcoord = new Array<Vector2>();
    Array<Vector2> tristrip = new Array<Vector2>();
    int batchSize;
    Vector2 perp = new Vector2();
    public float thickness = 30f;
    public float endcap = 8.5f;
    public Color color = new Color(Color.WHITE);
    private float alpha = 1f;
    ImmediateModeRenderer20 gl20;
    private SwipeHandler swipe;
    private Vector2 lastPoint = new Vector2();
    public SwipeTriStrip(SwipeHandler swipe) {
        this.swipe = swipe;
        gl20 = new ImmediateModeRenderer20(false, true, 1);
    }

    public void draw(Camera cam) {
        if (tristrip.size <= 0)
            return;

        gl20.begin(cam.combined, GL20.GL_TRIANGLE_STRIP);
        for (int i = 0; i < tristrip.size; i++) {
            if (i == batchSize) {
                gl20.end();
                gl20.begin(cam.combined, GL20.GL_TRIANGLE_STRIP);
            }
            Vector2 point = tristrip.get(i);
            Vector2 tc = texcoord.get(i);
            gl20.color(color.r, color.g, color.b, alpha);
            gl20.texCoord(tc.x, 0f);
            gl20.vertex(point.x, point.y, 0f);
        }
        gl20.end();
    }

    private int generate(Array<Vector2> input, int mult) {
        int c = tristrip.size;
        if (endcap <= 0) {
            tristrip.add(input.get(0));
        } else {
            Vector2 p = input.get(0);
            Vector2 p2 = input.get(1);
            perp.set(p).sub(p2).scl(endcap);
            tristrip.add(new Vector2(p.x + perp.x, p.y + perp.y));
        }
        texcoord.add(new Vector2(0f, 0f));

        for (int i = 1; i < input.size - 1; i++) {
            Vector2 p = input.get(i);
            Vector2 p2 = input.get(i + 1);

            //get direction and normalize it
            perp.set(p).sub(p2).nor();

            //get perpendicular
            perp.set(-perp.y, perp.x);

            float thick = thickness * (1f - ((i) / (float) (input.size)));

            //move outward by thickness
            perp.scl(thick / 2f);

            //decide on which side we are using
            perp.scl(mult);

            //add the tip of perpendicular
            tristrip.add(new Vector2(p.x + perp.x, p.y + perp.y));
            //0.0 -> end, transparent
            texcoord.add(new Vector2(0f, 0f));

            //add the center point
            tristrip.add(new Vector2(p.x, p.y));
            //1.0 -> center, opaque
            texcoord.add(new Vector2(1f, 0f));
        }

        //final point
        if (endcap <= 0) {
            tristrip.add(input.get(input.size - 1));
        } else {
            Vector2 p = input.get(input.size - 2);
            Vector2 p2 = input.get(input.size - 1);
            perp.set(p2).sub(p).scl(endcap);
            tristrip.add(new Vector2(p2.x + perp.x, p2.y + perp.y));
        }
        //end cap is transparent
        texcoord.add(new Vector2(0f, 0f));
        return tristrip.size - c;
    }

    public void update(Array<Vector2> input) {
        tristrip.clear();
        texcoord.clear();
        if (swipe.isDrawing() && !inputInPolygon(swipe.lastPoint, lastPoint, 2f)) {
            alpha = 1f;
        } else {
            alpha -= 0.08f;
            if (alpha < 0)
                alpha = 0;
        }
        lastPoint = swipe.lastPoint;
        if (input.size < 2 || alpha < .1f){
            return;
        }
        batchSize = generate(input, 1);
        int b = generate(input, -1);
    }

    private boolean inputInPolygon(Vector2 input, Vector2 polygonBase, float border){
        Polygon polygon = new Polygon(new float[]{polygonBase.x-border, polygonBase.y-border, polygonBase.x+border, polygonBase.y-border, polygonBase.x+border, polygonBase.y+border,polygonBase.x-border,polygonBase.y+border});
        return Intersector.isPointInPolygon(polygon.getVertices(), 0, 8, input.x, input.y);
    }

}