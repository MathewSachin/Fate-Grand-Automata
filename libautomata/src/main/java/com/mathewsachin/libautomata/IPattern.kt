package com.mathewsachin.libautomata

import java.io.OutputStream

/**
 * Interface for image objects.
 */
interface IPattern : AutoCloseable {
    val width: Int
    val height: Int

    /**
     * Creates a resized image with the specified size.
     *
     * @param Size the size of the new image
     */
    fun resize(Size: Size): IPattern

    /**
     * Creates a resized image with the specified size and writes it into the target.
     *
     * @param Target the image to write the resized image data to
     * @param Size the size of the new image
     */
    fun resize(Target: IPattern, Size: Size)

    /**
     * Checks if the given image matches with a high enough similarity value.
     *
     * @param Template the image to match with
     * @param Similarity the minimum similarity
     */
    fun isMatch(Template: IPattern, Similarity: Double): Boolean

    /**
     * Finds all image matches with high enough similarity values.
     *
     * @param Template the image to match with
     * @param Similarity the minimum similarity
     *
     * @return a list of [Match] objects
     */
    fun findMatches(Template: IPattern, Similarity: Double): Sequence<Match>

    /**
     * Crops the image to be within the bounds of the given [Region].
     *
     * Note that the resulting [IPattern] can have a smaller size than the [Region] if the [Region]
     * is not fully contained in the area of the image.
     *
     * @param Region a [Region] in image coordinates, see [Region.transformToImage]
     */
    fun crop(Region: Region): IPattern

    /**
     * Saves the image data to the given file path.
     *
     * @param stream an [OutputStream] to write the image into. PNG format is expected.
     */
    fun save(stream: OutputStream)

    /**
     * Makes a copy of the image.
     */
    fun copy(): IPattern
}

/**
 * Gets the width and height in the form of a [Size] object.
 */
val IPattern.Size get() = Size(width, height)