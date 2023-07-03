package org.luke.diminou.abs.animation.easing;

import androidx.annotation.NonNull;

import org.luke.diminou.abs.utils.ErrorHandler;

public class Spline implements Interpolator {

	private final float x1;
	private final float y1;
	private final float x2;
	private final float y2;

	private final boolean isCurveLinear;

	private static final int SAMPLE_SIZE = 16;
	private static final float SAMPLE_INCREMENT = 1.0f / SAMPLE_SIZE;
	private final float[] xSamples = new float[SAMPLE_SIZE + 1];

	Spline(float px1, float py1, float px2, float py2) {

		this.x1 = px1;
		this.y1 = py1;
		this.x2 = px2;
		this.y2 = py2;

		isCurveLinear = ((x1 == y1) && (x2 == y2));

		if (!isCurveLinear) {
			for (int i = 0; i < SAMPLE_SIZE + 1; ++i) {
				xSamples[i] = eval(i * SAMPLE_INCREMENT, x1, x2);
			}
		}
	}

	@Override
	public float interpolate(float x) {
		if (x < 0.0f || x > 1.0f) {
			ErrorHandler.handle(new IllegalArgumentException("x must be in range [0,1] : " + x), "creating spline interpolator");
		}

		if (isCurveLinear || x == 0.0f || x == 1.0f) {
			return x;
		}

		return eval(findTForX(x), y1, y2);
	}

	private float eval(float t, float p1, float p2) {
		float compT = 1 - t;
		return t * (3 * compT * (compT * p1 + t * p2) + (t * t));
	}

	private float evalDerivative(float t, float p1, float p2) {
		float compT = 1 - t;
		return 3 * (compT * (compT * p1 + 2 * t * (p2 - p1)) + t * t * (1 - p2));
	}

	private float getInitialGuessForT(float x) {
		for (int i = 1; i < SAMPLE_SIZE + 1; ++i) {
			if (xSamples[i] >= x) {
				float xRange = xSamples[i] - xSamples[i - 1];
				if (xRange == 0) {
					return (i - 1) * SAMPLE_INCREMENT;
				} else {
					return ((i - 1) + ((x - xSamples[i - 1]) / xRange)) * SAMPLE_INCREMENT;
				}
			}
		}
		return 1;
	}

	private float findTForX(float x) {
		float t = getInitialGuessForT(x);
		final int numIterations = 4;
		for (int i = 0; i < numIterations; ++i) {
			float xT = (eval(t, x1, x2) - x);
			float dXdT = evalDerivative(t, x1, x2);
			if (xT == 0 || dXdT == 0) {
				break;
			}
			t -= xT / dXdT;
		}
		return t;
	}

	@NonNull
	@Override
	public String toString() {
		return "SplineInterpolator [x1=" + x1 + ", y1=" + y1 + ", x2=" + x2 + ", y2=" + y2 + "]";
	}

}
