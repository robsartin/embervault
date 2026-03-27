/**
 * MVVM ViewModels: presentation-logic classes exposing JavaFX observable properties.
 *
 * <p>ViewModels orchestrate user interactions by calling inbound ports and
 * transforming domain data into a form suitable for display. They expose
 * {@code javafx.beans} properties and {@code javafx.collections} observables
 * that Views bind to declaratively.</p>
 *
 * <p>ViewModels must not reference {@code javafx.scene} classes (scene-graph
 * nodes, controls, layouts). This constraint is enforced by ArchUnit tests.</p>
 *
 * <p>See ADR-0013 (MVVM Design Pattern) and ADR-0009 (Hexagonal Architecture).</p>
 */
package com.embervault.adapter.in.ui.viewmodel;
