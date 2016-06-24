/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;

import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.WWUtil;

/**
 * Provides a mechanism for specifying images from a variety of sources. ImageSource retains the image source and its
 * associated type on behalf of the caller, making this information available to World Wind components that load images
 * on the caller's behalf.
 * <p/>
 * ImageSource supports four source types: <ul> <li>Android {@link android.graphics.Bitmap}</li> <li>World Wind {@link
 * ImageSource.BitmapFactory}</li> <li>Android resource identifier</li> <li>File path</li> <li>Uniform Resource Locator
 * (URL)</li> </ul>
 * <p/>
 * ImageSource instances are intended to be used as a key into a cache or other data structure that enables sharing of
 * loaded images.  Android bitmaps and World Wind bitmap factories are compared by reference: two image sources are
 * equivalent if they reference the same bitmap or the same bitmap factory. Android resource identifiers with equivalent
 * IDs are considered equivalent, as are file paths and URLs with the same string representation.
 */
public class ImageSource {

    /**
     * Factory for delegating construction of bitmap images. Bitmap factory provides a mechanism for World Wind
     * components to manage bitmap memory by specifying bitmaps indirectly, rather than specifying a reference to a
     * Bitmap object. The factory controls the bitmap contents, while World Wind controls the bitmap's lifecycle. This
     * enables World Wind to lazily construct bitmaps only when needed, cache those bitmaps, then release them from
     * memory when they're no longer needed. Additionally, bitmap factory enables World Wind to re-create bitmaps as
     * needed.
     */
    public interface BitmapFactory {

        /**
         * TODO convey that this is called lazily Returns the bitmap associated with this factory. The returned bitmap
         * is owned and managed by World Wind. The factory must not retain a reference to the bitmap and must not
         * recycle the bitmap.
         *
         * @return the bitmap
         */
        Bitmap createBitmap();
    }

    protected static final int TYPE_UNRECOGNIZED = 0;

    protected static final int TYPE_BITMAP = 1;

    protected static final int TYPE_BITMAP_FACTORY = 2;

    protected static final int TYPE_RESOURCE = 3;

    protected static final int TYPE_FILE_PATH = 4;

    protected static final int TYPE_URL = 5;

    protected int type = TYPE_UNRECOGNIZED;

    protected Object source;

    protected ImageSource() {
    }

    /**
     * Constructs an image source with a bitmap. The bitmap's dimensions should be no greater than 2048 x 2048.
     *
     * @param bitmap the bitmap to use as an image source
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the bitmap is null or recycled
     */
    public static ImageSource fromBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromBitmap", (bitmap == null) ? "missingBitmap" : "invalidBitmap"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_BITMAP;
        imageSource.source = bitmap;
        return imageSource;
    }

    /**
     * Constructs an image source with a bitmap factory. The factory must create images with dimensions to no greater
     * than 2048 x 2048.
     *
     * @param factory the bitmap factory to use as an image source
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the factory is null
     */
    public static ImageSource fromBitmapFactory(ImageSource.BitmapFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromBitmapFactory", "missingFactory"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_BITMAP_FACTORY;
        imageSource.source = factory;
        return imageSource;
    }

    /**
     * Constructs an image source with an Android resource identifier. The resource must be accessible from the Android
     * Context associated with the World Window, and its dimensions should be no greater than 2048 x 2048.
     *
     * @param id the resource identifier, as generated by the aapt tool
     *
     * @return the new image source
     */
    public static ImageSource fromResource(@DrawableRes int id) {
        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_RESOURCE;
        imageSource.source = id;
        return imageSource;
    }

    /**
     * Constructs an image source with a file path.
     *
     * @param pathName complete path name to the file
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the path name is null
     */
    public static ImageSource fromFilePath(String pathName) {
        if (pathName == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromFilePath", "missingPathName"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_FILE_PATH;
        imageSource.source = pathName;
        return imageSource;
    }

    /**
     * Constructs an image source with a URL string. The image's dimensions should be no greater than 2048 x 2048. The
     * application's manifest must include the permissions that allow network connections.
     *
     * @param urlString complete URL string
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the URL string is null
     */
    public static ImageSource fromUrl(String urlString) {
        if (urlString == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromUrl", "missingUrl"));
        }

        ImageSource imageSource = new ImageSource();
        imageSource.type = TYPE_URL;
        imageSource.source = urlString;
        return imageSource;
    }

    /**
     * Constructs an image source with a generic Object instance. The source may be any non-null Object. This is
     * equivalent to calling one of ImageSource's type-specific factory methods when the source is a recognized type:
     * bitmap; bitmap factory; integer resource ID; file path; URL string.
     *
     * @param source the generic source
     *
     * @return the new image source
     *
     * @throws IllegalArgumentException If the source is null
     */
    public static ImageSource fromObject(Object source) {
        if (source == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "ImageSource", "fromObject", "missingSource"));
        }

        if (source instanceof Bitmap) {
            return fromBitmap((Bitmap) source);
        } else if (source instanceof ImageSource.BitmapFactory) {
            return fromBitmapFactory((ImageSource.BitmapFactory) source);
        } else if (source instanceof Integer) { // Android resource identifier, as generated by the aapt tool
            return fromResource((Integer) source);
        } else if (source instanceof String && WWUtil.isUrlString((String) source)) {
            return fromUrl((String) source);
        } else if (source instanceof String) {
            return fromFilePath((String) source);
        } else {
            ImageSource imageSource = new ImageSource();
            imageSource.type = TYPE_UNRECOGNIZED;
            imageSource.source = source;
            return imageSource;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        ImageSource that = (ImageSource) o;
        return this.type == that.type && this.source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return this.type + 31 * this.source.hashCode();
    }

    @Override
    public String toString() {
        if (this.type == TYPE_BITMAP) {
            return "Bitmap " + this.source.toString();
        } else if (this.type == TYPE_BITMAP_FACTORY) {
            return "BitmapFactory " + this.source.toString();
        } else if (this.type == TYPE_RESOURCE) {
            return "Resource " + this.source.toString();
        } else if (this.type == TYPE_FILE_PATH) {
            return this.source.toString();
        } else if (this.type == TYPE_URL) {
            return this.source.toString();
        } else {
            return this.source.toString();
        }
    }

    /**
     * Indicates whether this image source is a Bitmap.
     *
     * @return true if the source is a Bitmap, otherwise false
     */
    public boolean isBitmap() {
        return this.type == TYPE_BITMAP;
    }

    /**
     * Indicates whether this image source is a bitmap factory.
     *
     * @return true if the source is a bitmap factory, otherwise false
     */
    public boolean isBitmapFactory() {
        return this.type == TYPE_BITMAP_FACTORY;
    }

    /**
     * Indicates whether this image source is an Android resource.
     *
     * @return true if the source is an Android resource, otherwise false
     */
    public boolean isResource() {
        return this.type == TYPE_RESOURCE;
    }

    /**
     * Indicates whether this image source is a file path.
     *
     * @return true if the source is an file path, otherwise false
     */
    public boolean isFilePath() {
        return this.type == TYPE_FILE_PATH;
    }

    /**
     * Indicates whether this image source is a URL string.
     *
     * @return true if the source is a URL string, otherwise false
     */
    public boolean isUrl() {
        return this.type == TYPE_URL;
    }

    /**
     * Returns the source bitmap. Call isBitmap to determine whether or not the source is a bitmap.
     *
     * @return the bitmap, or null if the source is not a bitmap
     */
    public Bitmap asBitmap() {
        return (this.type == TYPE_BITMAP) ? (Bitmap) this.source : null;
    }

    /**
     * Returns the source bitmap factory. Call isBitmapFactory to determine whether or not the source is a bitmap
     * factory.
     *
     * @return the bitmap factory, or null if the source is not a bitmap factory
     */
    public ImageSource.BitmapFactory asBitmapFactory() {
        return (this.type == TYPE_BITMAP_FACTORY) ? (ImageSource.BitmapFactory) this.source : null;
    }

    /**
     * Returns the source Android resource identifier. Call isResource to determine whether or not the source is an
     * Android resource.
     *
     * @return the resource identifier as generated by the aapt tool, or null if the source is not an Android resource
     */
    @DrawableRes
    public int asResource() {
        return (this.type == TYPE_RESOURCE) ? (int) this.source : 0;
    }

    /**
     * Returns the source file path name. Call isFilePath to determine whether or not the source is a file path.
     *
     * @return the file path name, or null if the source is not a file path
     */
    public String asFilePath() {
        return (this.type == TYPE_FILE_PATH) ? (String) this.source : null;
    }

    /**
     * Returns the source URL string. Call isUrl to determine whether or not the source is a URL string.
     *
     * @return the URL string, or null if the source is not a URL string
     */
    public String asUrl() {
        return (this.type == TYPE_URL) ? (String) this.source : null;
    }

    /**
     * Returns the image source associated with an unrecognized type.
     *
     * @return the source object
     */
    public Object asObject() {
        return this.source;
    }
}
