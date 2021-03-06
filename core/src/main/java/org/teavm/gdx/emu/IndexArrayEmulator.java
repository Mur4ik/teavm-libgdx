
package org.teavm.gdx.emu;

import java.nio.ShortBuffer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class IndexArrayEmulator implements IndexData {
	ShortBuffer buffer;
	int bufferHandle;
	final boolean isDirect;
	boolean isDirty = true;
	boolean isBound = false;
	final int usage;

	/** Creates a new IndexBufferObject.
	 *
	 * @param isStatic whether the index buffer is static
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public IndexArrayEmulator (boolean isStatic, int maxIndices) {
		isDirect = true;
		buffer = BufferUtils.newShortBuffer(maxIndices);
		buffer.flip();
		bufferHandle = Gdx.gl20.glGenBuffer();
		usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
	}

	/** Creates a new IndexBufferObject to be used with vertex arrays.
	 *
	 * @param maxIndices the maximum number of indices this buffer can hold */
	public IndexArrayEmulator (int maxIndices) {
		this.isDirect = true;
		buffer = BufferUtils.newShortBuffer(maxIndices);
		buffer.flip();
		bufferHandle = Gdx.gl20.glGenBuffer();
		usage = GL20.GL_STATIC_DRAW;
	}

	/** @return the number of indices currently stored in this buffer */
	@Override
	public int getNumIndices () {
		return buffer.limit();
	}

	/** @return the maximum number of indices this IndexBufferObject can store. */
	@Override
	public int getNumMaxIndices () {
		return buffer.capacity();
	}

	/** <p>
	 * Sets the indices of this IndexBufferObject, discarding the old indices. The count must equal the number of indices to be
	 * copied to this IndexBufferObject.
	 * </p>
	 *
	 * <p>
	 * This can be called in between calls to {@link #bind()} and {@link #unbind()}. The index data will be updated instantly.
	 * </p>
	 *
	 * @param indices the vertex data
	 * @param offset the offset to start copying the data from
	 * @param count the number of shorts to copy */
	@Override
	public void setIndices (short[] indices, int offset, int count) {
		isDirty = true;
		buffer.clear();
		buffer.put(indices, offset, count);
		buffer.flip();

		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
			isDirty = false;
		}
	}

	@Override
	public void setIndices (ShortBuffer indices) {
		isDirty = true;
		buffer.clear();
		buffer.put(indices);
		buffer.flip();

		if (isBound) {
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
			isDirty = false;
		}
	}

	/** <p>
	 * Returns the underlying ShortBuffer. If you modify the buffer contents they wil be uploaded on the call to {@link #bind()}.
	 * If you need immediate uploading use {@link #setIndices(short[], int, int)}.
	 * </p>
	 *
	 * @return the underlying short buffer. */
	@Override
	public ShortBuffer getBuffer () {
		isDirty = true;
		return buffer;
	}

	/** Binds this IndexBufferObject for rendering with glDrawElements. */
	@Override
	public void bind () {
		if (bufferHandle == 0) throw new GdxRuntimeException("No buffer allocated!");

		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, bufferHandle);
		if (isDirty) {
			Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, buffer.limit(), buffer, usage);
			isDirty = false;
		}
		isBound = true;
	}

	/** Unbinds this IndexBufferObject. */
	@Override
	public void unbind () {
		Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		isBound = false;
	}

	/** Invalidates the IndexBufferObject so a new OpenGL buffer handle is created. Use this in case of a context loss. */
	@Override
	public void invalidate () {
		bufferHandle = Gdx.gl20.glGenBuffer();
		isDirty = true;
	}

	/** Disposes this IndexBufferObject and all its associated OpenGL resources. */
	@Override
	public void dispose () {
		GL20 gl = Gdx.gl20;
		gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
		gl.glDeleteBuffer(bufferHandle);
		bufferHandle = 0;
	}
}
