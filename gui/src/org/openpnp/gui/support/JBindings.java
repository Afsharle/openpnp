package org.openpnp.gui.support;

import java.awt.Color;

import javax.swing.JComponent;

import org.jdesktop.beansbinding.AbstractBindingListener;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.BindingListener;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.Converter;
import org.openpnp.model.AbstractModelObject;

/**
 * Provides convenience bindings for JComponents that add value buffering and
 * visual feedback on conversion failure. Buffered values have a read-write
 * binding with the JComponent and a read binding with the source value. The
 * returned WrappedBinding allows you to save the buffered value to the source
 * or to reset it from the source.
 * @author Jason von Nieda <jason@vonnieda.org>
 */
public class JBindings {
	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(
			SS source, 
			String sourcePropertyName, 
			TS component, 
			String targetPropertyName) {
		return new WrappedBinding<SS, SV, TS, TV>(source, sourcePropertyName, component, targetPropertyName, null, null);
	}
	
	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(
			SS source, 
			String sourcePropertyName, 
			TS component, 
			String targetPropertyName, 
			Converter<SV, TV> converter) {
		return new WrappedBinding<SS, SV, TS, TV>(source, sourcePropertyName, component, targetPropertyName, converter, null);
	}

	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(
			SS source, 
			String sourcePropertyName, 
			TS component, 
			String targetPropertyName, 
			Converter<SV, TV> converter,
			BindingListener listener) {
		return new WrappedBinding<SS, SV, TS, TV>(source, sourcePropertyName, component, targetPropertyName, converter, listener);
	}

	public static <SS, SV, TS extends JComponent, TV> WrappedBinding<SS, SV, TS, TV> bind(
			SS source, 
			String sourcePropertyName, 
			TS component, 
			String targetPropertyName, 
			BindingListener listener) {
		return new WrappedBinding<SS, SV, TS, TV>(source, sourcePropertyName, component, targetPropertyName, null, listener);
	}

	public static class WrappedBinding<SS, SV, TS extends JComponent, TV> {
		private SS source;
		private BeanProperty<SS, SV> sourceProperty;
		private Wrapper<SV> wrapper;
		
		public WrappedBinding(
				SS source, 
				String sourcePropertyName, 
				TS component, 
				String targetPropertyName, 
				Converter<SV, TV> converter,
				BindingListener listener) {
			this.source = source;
			this.sourceProperty = BeanProperty.create(sourcePropertyName);
			this.wrapper = new Wrapper<SV>(sourceProperty.getValue(source));
			BeanProperty<Wrapper<SV>, SV> wrapperProperty = BeanProperty.create("value");
			BeanProperty<TS, TV> targetProperty = BeanProperty.create(targetPropertyName);
			AutoBinding<Wrapper<SV>, SV, TS, TV> wrappedBinding = Bindings.createAutoBinding(
					UpdateStrategy.READ_WRITE, 
					wrapper,
					wrapperProperty, 
					component, 
					targetProperty);
			if (converter != null) {
				wrappedBinding.setConverter(converter);
			}
			wrappedBinding.addBindingListener(new JComponentBackgroundUpdater(component));
			if (listener != null) {
				wrappedBinding.addBindingListener(listener);
			}
			wrappedBinding.bind();
			AutoBinding<SS, SV, Wrapper<SV>, SV> binding = Bindings.createAutoBinding(
					UpdateStrategy.READ, 
					source,
					sourceProperty, 
					wrapper, 
					wrapperProperty);
			binding.bind();
		}
		
		public void save() {
			sourceProperty.setValue(source, wrapper.getValue());
		}
		
		public void reset() {
			wrapper.setValue(sourceProperty.getValue(source));
		}
	}
	
	public static class Wrapper<T> extends AbstractModelObject {
		private T value;
		
		public Wrapper(T value) {
			this.value = value;
		}
		
		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			T oldValue = this.value;
			this.value = value;
			firePropertyChange("value", oldValue, this.value);
		}
	}
	
	private static class JComponentBackgroundUpdater extends AbstractBindingListener {
		private static Color errorColor = new Color(0xff, 0xdd, 0xdd);
		private JComponent component;
		private Color oldBackground;
		
		public JComponentBackgroundUpdater(JComponent component) {
			this.component = component;
			oldBackground = component.getBackground();
		}
		
		@Override
		public void syncFailed(Binding binding, SyncFailure failure) {
			component.setBackground(errorColor);
		}

		@Override
		public void synced(Binding binding) {
			component.setBackground(oldBackground);
		}
	}
}
