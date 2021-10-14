---
description: >-
  This Export Plugin for Goobi workflow executes a specific export of Goobi processes as multiple METS files per process which was developed for the Federal Monuments Office in Austria. Each structure element results in its own METS file. 
  
---

goobi-plugin-export-bda-singleImage
===========================================================================


Introduction
---------------------------------------------------------------------------
This plugin is used for a special export of multiple METS files per process. From a single METS file within Goobi workflow, a separate METS file with the associated image files is created during the export for each structural element contained.

This plugin was developed for the Federal Office for the Protection of Monuments in Austria and is functionally geared to their needs and therefore may not be directly applicable to other use cases.


Overview
---------------------------------------------------------------------------

Details             |  Description
------------------- | -----------------------------------------------------
Identifier          | intranda_export_singleImage
Source code         | [https://github.com/intranda/goobi-plugin-export-bda-singleImage](https://github.com/intranda/goobi-plugin-export-bda-singleImage)
License             | GPL 2.0 or newer 
Documentation date  | 13.10.2021


Installation
---------------------------------------------------------------------------
The plugin consists of the following files to be installed:

```text
plugin_intranda_export_bdaSingleImage.jar
```

This file must be installed in the following directory:

```bash
/opt/digiverso/goobi/plugins/export/plugin_intranda_export_bdaSingleImage.jar
```


Configuration
---------------------------------------------------------------------------
This plugin does not have its own configuration file.



Integration of the plugin into the workflow
---------------------------------------------------------------------------
To put the plugin into operation, it must be activated for one or more desired tasks in the workflow. This is done as shown in the following screenshot by selecting the plugin `intranda_export_singleImage` from the list of installed plugins.

![Assigning the plugin to a specific task](intranda_export_bda_singleImage_en.png)

Since this plugin should usually be executed automatically, the workflow step should be configured as `automatic`. In addition, the task must be marked as an export step.



How the plugin works
---------------------------------------------------------------------------
Once the plugin has been fully installed and set up, it is usually run automatically within the workflow, so there is no manual interaction with the user. Instead, calling the plugin through the workflow in the background does the following: 

For each structural element within the METS file, an independent METS file is created during the export, to which the respective image files are exported along with it. The entire METS file is not exported. The number of METS files created in this way differs accordingly from the number of Goobi processes and corresponds to the number of existing structural elements.