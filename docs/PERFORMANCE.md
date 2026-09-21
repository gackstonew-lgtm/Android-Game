# Mobile Performance Architecture - Chase

This document outlines memory management, rendering optimizations, and frame-rate budgeting for high-performance mobile execution.

---

## 1. Frame Budget & Target Rates

* **Target Frame Rate**: 60 FPS (16.6ms per frame budget) across mid-range to flagship devices.
* **Low-End Target**: Minimum stable 30 FPS on entry-level Android devices (e.g. 2GB RAM / Adreno 610).

---

## 2. Memory & Garbage Collection (GC) Strategy

* **Zero Per-Frame Heap Allocations**:
  * Math vectors (`Vector3`, `Vector2`, `Matrix4`, `BoundingBox`) are pre-allocated as class members or recycled through `ObjectPoolManager`.
  * Spatial collision queries reuse cached arrays (`activePairs`, `collidables`) without instantiating new iterators or wrapper objects.
  * String allocations in HUD loops use pre-formatted string buffers and conditional dirty-flag updates.

---

## 3. Rendering Pipeline Optimizations

* **Batching**: 3D meshes are submitted using a single `ModelBatch` per pass, minimizing state changes and draw calls.
* **Segment Recycling**: Road and guardrail instances are dynamically shifted from behind the player to the front of the track, keeping memory usage constant regardless of distance traveled.
* **Atmospheric Fog**: Limits visual draw distance to 300 units (`CAMERA_FAR`), allowing aggressive back-face and distance culling.
* **MSAA Sampling**: Limited to 2x multi-sampling on mobile GPUs to balance crisp edges with fill-rate efficiency.

---

## 4. Graphics Presets

| Quality Preset | Fog Distance | MSAA Samples | Texture Filtering | Target Frame Rate |
| :--- | :---: | :---: | :---: | :---: |
| **LOW** | 180m | Off (0x) | Bilinear | 30 - 60 FPS |
| **MEDIUM** | 240m | 2x | Bilinear | 60 FPS |
| **HIGH** | 300m | 2x | Trilinear / Anisotropic | 60+ FPS |
