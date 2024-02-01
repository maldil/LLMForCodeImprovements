# GPTForCodeImprovements

## Overview

"GPTForCodeImprovements" is an innovative open-source project aimed at leveraging the power of advanced Generative Pre-trained Transformer (GPT) models to automate the process of code review and enhance code quality. This tool is designed to provide developers and teams with intelligent recommendations for code optimizations, bug identifications, and best coding practices tailored to their projects.

## Objectives

- **Automate Code Reviews:** Use GPT models to automatically analyze code repositories, identifying areas for improvement and suggesting specific optimizations.
- **Enhance Code Quality:** Elevate the overall quality of codebases through optimization suggestions and adherence to best practices, making code more robust, readable, and performant.
- **Educate Developers:** Help developers learn best practices and more efficient coding methods by comparing their code against the GPT model's recommendations.
- **Streamline Development Workflows:** Seamlessly integrate with existing development workflows and tools to provide real-time feedback and suggestions, reducing the time spent on manual code reviews.

## APIS
- [GenerateImprovements](https://github.com/maldil/LLMForCodeImprovements/blob/main/src/main/java/org/mal/GenerateImprovements.java#L4) generates code improvement suggestions for the methods in the projects defined in the file [selected_repos_updated.txt](https://github.com/maldil/LLMForCodeImprovements/blob/main/selected_repos_updated.txt)
- [ApplyMain](https://github.com/maldil/LLMForCodeImprovements/blob/main/src/main/java/org/mal/apply/ApplyMain.java#L6) applies the improvements generated in the preceding step and reports compile errors.
- [AnalyseStats](https://github.com/maldil/LLMForCodeImprovements/blob/main/src/main/java/org/mal/stats/AnalyseStats.java) statistically analyzes the improvement suggestions and compile errors.
- [GroupImprovements](https://github.com/maldil/LLMForCodeImprovements/blob/main/src/main/java/org/mal/stats/GroupImprovements.java) analyzes and groups improvements based on regular expressions.
