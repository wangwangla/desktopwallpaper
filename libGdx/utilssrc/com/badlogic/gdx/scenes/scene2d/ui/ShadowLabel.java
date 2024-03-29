//package com.badlogic.gdx.scenes.scene2d.ui;
//
///*******************************************************************************
// * Copyright 2011 See AUTHORS file.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *   http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// ******************************************************************************/
//
//import com.badlogic.gdx.graphics.Color;
//import com.badlogic.gdx.graphics.g2d.Batch;
//import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
//import com.badlogic.gdx.graphics.g2d.GlyphLayout;
//import com.badlogic.gdx.math.Vector2;
//import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
//import com.badlogic.gdx.utils.Align;
//import com.badlogic.gdx.utils.StringBuilder;
//
///** A text com.kw.gdx.label, with optional word wrapping.
// * <p>
// * The preferred size of the com.kw.gdx.label is determined by the actual text bounds, unless {@link #setWrap(boolean) word wrap} is enabled.
// * @author Nathan Sweet */
//public class ShadowLabel extends Widget {
//    static private final Color tempColor = new Color();
//    static private final GlyphLayout4 prefSizeLayout = new GlyphLayout4();
//
//    private Label.LabelStyle style;
//    private final GlyphLayout4 layout = new GlyphLayout4();
//    private final Vector2 prefSize = new Vector2();
//    private final StringBuilder text = new StringBuilder();
//    private BitmapFontCache cache;
//    private int labelAlign = Align.left;
//    private int lineAlign = Align.left;
//    private boolean wrap;
//    private float lastPrefHeight;
//    private boolean prefSizeInvalid = true;
//    private float fontScaleX = 1, fontScaleY = 1;
//    private boolean fontScaleChanged = false;
//    private String ellipsis;
//    private float shadowOffsetX,shadowOffsetY;
//    private Color shadowColor = new Color(0,0,0,1);
//    private float modkern;
//
//    private float modLineHeight;
//
//    public ShadowLabel (CharSequence text, Skin skin) {
//        this(text, skin.get(Label.LabelStyle.class));
//    }
//
//    public ShadowLabel (CharSequence text, Skin skin, String styleName) {
//        this(text, skin.get(styleName, Label.LabelStyle.class));
//    }
//
//    /** Creates a com.kw.gdx.label, using a {@link Label.LabelStyle} that has a BitmapFont with the specified name from the skin and the specified
//     * color. */
//    public ShadowLabel (CharSequence text, Skin skin, String fontName, Color color) {
//        this(text, new Label.LabelStyle(skin.getFont(fontName), color));
//    }
//
//    /** Creates a com.kw.gdx.label, using a {@link Label.LabelStyle} that has a BitmapFont with the specified name and the specified color from the
//     * skin. */
//    public ShadowLabel (CharSequence text, Skin skin, String fontName, String colorName) {
//        this(text, new Label.LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
//    }
//
//    public ShadowLabel (CharSequence text, Label.LabelStyle style) {
//        if (text != null) this.text.append(text);
//        setStyle(style);
//        if (text != null && text.length() > 0) setSize(getPrefWidth(), getPrefHeight());
//    }
//
//    public void setStyle (Label.LabelStyle style) {
//        if (style == null) throw new IllegalArgumentException("style cannot be null.");
//        if (style.font == null) throw new IllegalArgumentException("Missing LabelStyle font.");
//        this.style = style;
//        cache = style.font.newFontCache();
//        invalidateHierarchy();
//    }
//
//    /** Returns the com.kw.gdx.label's style. Modifying the returned style may not have an com.kw.gdx.animation.effect until {@link #setStyle(Label.LabelStyle)} is
//     * called. */
//    public Label.LabelStyle getStyle () {
//        return style;
//    }
//
//    /** @param newText May be null, "" will be used. */
//    public void setText (CharSequence newText) {
//        if (newText == null) newText = "";
//        if (newText instanceof StringBuilder) {
//            if (text.equals(newText)) return;
//            text.setLength(0);
//            text.append((StringBuilder)newText);
//        } else {
//            if (textEquals(newText)) return;
//            text.setLength(0);
//            text.append(newText);
//        }
//        invalidateHierarchy();
//    }
//
//    public void setShadowOffset(float shadowOffsetX,float shadowOffsetY) {
//        this.shadowOffsetX = shadowOffsetX;
//        this.shadowOffsetY = shadowOffsetY;
//    }
//
//    public void setShadowColor(float r,float g,float b,float a){
//        shadowColor.set(r,g,b,a);
//    }
//    public void setShadowColor(Color color){
//        shadowColor.set(color);
//    }
//
//    public boolean textEquals (CharSequence other) {
//        int length = text.length;
//        char[] chars = text.chars;
//        if (length != other.length()) return false;
//        for (int i = 0; i < length; i++)
//            if (chars[i] != other.charAt(i)) return false;
//        return true;
//    }
//
//    public StringBuilder getText () {
//        return text;
//    }
//
//    public void invalidate () {
//        super.invalidate();
//        prefSizeInvalid = true;
//    }
//
//    private void scaleAndComputePrefSize () {
//        BitmapFont font = cache.getFont();
//        float oldScaleX = font.getScaleX();
//        float oldScaleY = font.getScaleY();
//        if (fontScaleChanged) font.getData().setScale(fontScaleX, fontScaleY);
//
//        computePrefSize();
//
//        if (fontScaleChanged) font.getData().setScale(oldScaleX, oldScaleY);
//    }
//
//    private void computePrefSize () {
//        prefSizeInvalid = false;
//        GlyphLayout prefSizeLayout = ShadowLabel.prefSizeLayout;
//        if (wrap && ellipsis == null) {
//            float width = getWidth();
//            if (style.background != null) width -= style.background.getLeftWidth() + style.background.getRightWidth();
//            prefSizeLayout.setText(cache.getFont(), text, Color.WHITE, width, Align.left, true);
//        } else
//            prefSizeLayout.setText(cache.getFont(), text);
//        prefSize.set(prefSizeLayout.width, prefSizeLayout.height);
//    }
//
//    public void layout () {
//        BitmapFont font = cache.getFont();
//        float oldScaleX = font.getScaleX();
//        float oldScaleY = font.getScaleY();
//        if (fontScaleChanged) font.getData().setScale(fontScaleX, fontScaleY);
//
//        boolean wrap = this.wrap && ellipsis == null;
//        if (wrap) {
//            float prefHeight = getPrefHeight();
//            if (prefHeight != lastPrefHeight) {
//                lastPrefHeight = prefHeight;
//                invalidateHierarchy();
//            }
//        }
//
//        float width = getWidth(), height = getHeight();
//        Drawable background = style.background;
//        float x = 0, y = 0;
//        if (background != null) {
//            x = background.getLeftWidth();
//            y = background.getBottomHeight();
//            width -= background.getLeftWidth() + background.getRightWidth();
//            height -= background.getBottomHeight() + background.getTopHeight();
//        }
//
//        GlyphLayout layout = this.layout;
//        float textWidth, textHeight;
//        if (wrap || text.indexOf("\n") != -1) {
//            // If the text can span multiple lines, determine the text's actual size so it can be aligned within the com.kw.gdx.label.
//            layout.setText(font, text, 0, text.length, Color.WHITE, width, lineAlign, wrap, ellipsis);
//            textWidth = layout.width;
//            textHeight = layout.height;
//
//            if ((labelAlign & Align.left) == 0) {
//                if ((labelAlign & Align.right) != 0)
//                    x += width - textWidth;
//                else
//                    x += (width - textWidth) / 2;
//            }
//        } else {
//            textWidth = width;
//            textHeight = font.getData().capHeight;
//        }
//
//        if ((labelAlign & Align.top) != 0) {
//            y += cache.getFont().isFlipped() ? 0 : height - textHeight;
//            y += style.font.getDescent();
//        } else if ((labelAlign & Align.bottom) != 0) {
//            y += cache.getFont().isFlipped() ? height - textHeight : 0;
//            y -= style.font.getDescent();
//        } else {
//            y += (height - textHeight) / 2;
//        }
//        if (!cache.getFont().isFlipped()) y += textHeight;
//
//        layout.setText(font, text, 0, text.length, Color.WHITE, textWidth, lineAlign, wrap, ellipsis);
//        cache.setText(layout, x, y);
//
//        if (fontScaleChanged) font.getData().setScale(oldScaleX, oldScaleY);
//    }
//
//    public void draw (Batch batch, float parentAlpha) {
//        validate();
//        drawShadow(batch,parentAlpha);
//        Color color = tempColor.set(getColor());
//        color.a *= parentAlpha;
//        if (style.background != null) {
//            batch.setColor(color.r, color.g, color.b, color.a);
//            style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
//        }
//        if (style.fontColor != null) color.mul(style.fontColor);
//        cache.tint(color);
//        cache.setPosition(getX(), getY());
//        cache.draw(batch);
//    }
//
//    private void drawShadow(Batch batch, float parentAlpha){
//        if(shadowOffsetX == 0 && shadowOffsetY == 0) return;
//        Color color = tempColor.set(shadowColor);
//        if(getColor().a!=0){
//            color.a *=parentAlpha;
//        }else{
//            color.a =0;
//        }
//        batch.setColor(color);
//        cache.tint(color);
//        cache.setPosition(getX()+shadowOffsetX,getY()+shadowOffsetY);
//        cache.draw(batch);
//    }
//    public float getPrefWidth () {
//        if (wrap) return 0;
//        if (prefSizeInvalid) scaleAndComputePrefSize();
//        float width = prefSize.x;
//        Drawable background = style.background;
//        if (background != null) width += background.getLeftWidth() + background.getRightWidth();
//        return width;
//    }
//
//    public float getPrefHeight () {
//        if (prefSizeInvalid) scaleAndComputePrefSize();
//        float descentScaleCorrection = 1;
//        if (fontScaleChanged) descentScaleCorrection = fontScaleY / style.font.getScaleY();
//        float height = prefSize.y - style.font.getDescent() * descentScaleCorrection * 2;
//        Drawable background = style.background;
//        if (background != null) height += background.getTopHeight() + background.getBottomHeight();
//        return height;
//    }
//
//    public GlyphLayout getGlyphLayout () {
//        return layout;
//    }
//
//    public GlyphLayout getprefSizeLayout () {
//        return prefSizeLayout;
//    }
//
//    /** If false, the text will only wrap where it contains newlines (\n). The preferred size of the com.kw.gdx.label will be the text bounds.
//     * If true, the text will word wrap using the width of the com.kw.gdx.label. The preferred width of the com.kw.gdx.label will be 0, it is expected
//     * that something external will set the width of the com.kw.gdx.label. Wrapping will not occur when ellipsis is enabled. Default is false.
//     * <p>
//     * When wrap is enabled, the com.kw.gdx.label's preferred height depends on the width of the com.kw.gdx.label. In some cases the parent of the com.kw.gdx.label
//     * will need to layout twice: once to set the width of the com.kw.gdx.label and a second time to adjust to the com.kw.gdx.label's new preferred
//     * height. */
//    public void setWrap (boolean wrap) {
//        this.wrap = wrap;
//        invalidateHierarchy();
//    }
//
//    public int getLabelAlign () {
//        return labelAlign;
//    }
//
//    public int getLineAlign () {
//        return lineAlign;
//    }
//
//    /** @param alignment Aligns all the text within the com.kw.gdx.label (default left center) and each line of text horizontally (default
//     *           left).
//     * @see Align */
//    public void setAlignment (int alignment) {
//        setAlignment(alignment, alignment);
//    }
//
//    /** @param labelAlign Aligns all the text within the com.kw.gdx.label (default left center).
//     * @param lineAlign Aligns each line of text horizontally (default left).
//     * @see Align */
//    public void setAlignment (int labelAlign, int lineAlign) {
//        this.labelAlign = labelAlign;
//
//        if ((lineAlign & Align.left) != 0)
//            this.lineAlign = Align.left;
//        else if ((lineAlign & Align.right) != 0)
//            this.lineAlign = Align.right;
//        else
//            this.lineAlign = Align.center;
//
//        invalidate();
//    }
//
//    public void setFontScale (float fontScale) {
//        setFontScale(fontScale, fontScale);
//    }
//
//    public void setFontScale (float fontScaleX, float fontScaleY) {
//        fontScaleChanged = true;
//        this.fontScaleX = fontScaleX;
//        this.fontScaleY = fontScaleY;
//        invalidateHierarchy();
//    }
//
//    public float getFontScaleX () {
//        return fontScaleX;
//    }
//
//    public void setFontScaleX (float fontScaleX) {
//        setFontScale(fontScaleX, fontScaleY);
//    }
//
//    public float getFontScaleY () {
//        return fontScaleY;
//    }
//
//    public void setFontScaleY (float fontScaleY) {
//        setFontScale(fontScaleX, fontScaleY);
//    }
//
//    /** When non-null the text will be truncated "..." if it does not fit within the width of the com.kw.gdx.label. Wrapping will not occur
//     * when ellipsis is enabled. Default is false. */
//    public void setEllipsis (String ellipsis) {
//        this.ellipsis = ellipsis;
//    }
//
//    /** When true the text will be truncated "..." if it does not fit within the width of the com.kw.gdx.label. Wrapping will not occur when
//     * ellipsis is true. Default is false. */
//    public void setEllipsis (boolean ellipsis) {
//        if (ellipsis)
//            this.ellipsis = "...";
//        else
//            this.ellipsis = null;
//    }
//
//    /** Allows subclasses to access the cache in {@link #draw(Batch, float)}. */
//    protected BitmapFontCache getBitmapFontCache () {
//        return cache;
//    }
//
//    public String toString () {
//        return super.toString() + ": " + text;
//    }
//
//    public void setModkern (float modkern) {
//        this.modkern = modkern;
//        layout.setModkerning(modkern);
//        prefSizeLayout.setModkerning(modkern);
//        invalidateHierarchy();
//    }
//
//
//    public void setModLineHeight(float modLineHeight) {
//        this.modLineHeight = modLineHeight;
//        layout.setModLineHeight(modLineHeight);
//        prefSizeLayout.setModLineHeight(modLineHeight);
//        invalidateHierarchy();
//    }
//
//}
