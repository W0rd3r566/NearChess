# Material 3 references and profile

References consulted:
- https://m3.material.io/
- https://github.com/hamen/material-3-skill
- https://github.com/hamen/material-3-skill/blob/main/skills/material-3/SKILL.md

The community skill is an assistant reference, not an Android runtime dependency. It was read through GitHub; no installation is claimed. Its code/assets have not been vendored. Consult official Compose API documentation for exact APIs supported by the project BOM.

ProfileActivity uses Material 3 typography roles, paired color roles, standard buttons and fields, 16/24dp spacing, system/IME insets. Full application theme unification and accessibility audit are not complete.

Local profile supports nickname (1–24 characters) and a photo selected by PickVisualMedia. The photo is sampled, center cropped, resized to 512px and re-encoded to an app-private JPEG. No broad media permission, network upload or account. Original gallery media is never deleted. Custom image orientation/cropping UI is not yet implemented. Profile photo and nickname are not yet wired into game player panels or Bluetooth.
