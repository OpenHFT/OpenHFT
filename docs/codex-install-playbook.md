# Playbook: Setting up OpenAI Codex CLI on a new machine

This guide walks you from a clean machine to a working Codex CLI with global memory (`~/.codex/AGENTS.md`) and room for
future playbooks.

---

## 0. Prerequisites

Codex CLI officially supports macOS and Linux; Windows support is experimental and usually works best via WSL2.

Before installing:

- OS
    - macOS 12+ or a recent Linux (Ubuntu 20.04+/Debian 10+ recommended).
    - On Windows, install WSL2 and use an Ubuntu/Debian environment.
- Node.js
    - Install Node 22+ (LTS recommended).
    - Use `nvm` or your OS package manager.
- Git (recommended)
    - `git --version` should work so Codex can use Git for diffs and safety.

---

## 1. Install Codex CLI

You can install Codex globally via npm or Homebrew.

### Option A: npm (works everywhere with Node)

```bash
# Install globally
npm install -g @openai/codex

# Upgrade later
npm install -g @openai/codex@latest
```

### Option B: Homebrew (macOS only)

```bash
brew install --cask codex
# Upgrade later
brew upgrade codex
```

You can also download platform-specific binaries from the GitHub releases page if you prefer not to use npm/brew.

Verify installation:

```bash
codex --version
```

---

## 2. Authenticate Codex

Codex needs to be tied to your ChatGPT plan or an API key.

### 2.1. Log in with ChatGPT (recommended)

Run:

```bash
codex
```

On first launch, Codex opens a browser and prompts you to sign in with ChatGPT Plus / Pro / Business / Edu / Enterprise.

Follow the on-screen flow until Codex says you are authenticated.

You can later check status with:

```bash
codex login status
```

If the exit code is 0, you are logged in.

### 2.2. Use an OpenAI API key (usage-based billing)

If you prefer pure API-key auth:

1. Create an API key in your OpenAI account.
2. Export it in your shell:

   ```bash
   export OPENAI_API_KEY="sk-...your-key..."
   ```

3. Login via key:

   ```bash
   printenv OPENAI_API_KEY | codex login --with-api-key
   ```

Codex reads the key from stdin and stores credentials.

---

## 3. Create Codex config folder

Codex stores configuration under `~/.codex` (or `$CODEX_HOME` if set).

```bash
mkdir -p ~/.codex
```

### Optional: minimal `config.toml`

Create `~/.codex/config.toml`:

```toml
# ~/.codex/config.toml

# Default profile/model preference (example - adjust as needed)
[profiles.default]
model = "gpt-5.1-codex"

# Safer default approval mode: suggest changes, ask before edits/runs.
approval_mode = "suggest"
```

Codex reads this file automatically on startup to pick defaults.

---

## 4. Add global guidance with `~/.codex/AGENTS.md`

Codex uses AGENTS.md files as persistent instructions and context. It looks in:

1. `~/.codex/AGENTS.md` - global personal rules.
2. Project root `AGENTS.md`.
3. Deeper subfolders' `AGENTS.md`.

Files are layered top-down so global rules come first and project-specific ones override them.

Create `~/.codex/AGENTS.md`:

```markdown
# Global Codex Guidance

## Working agreements
- Prefer safe, incremental changes and explain non-obvious edits.
- Ask before running destructive git or system commands.
- For Java/Maven projects, favour `mvn verify` or module-scoped tests after code changes.

## Memory & context
- Assume that project-specific AGENTS.md files exist in repo roots and key modules.
- Honour any testing and style rules defined there.

## Workflow hygiene
- When you discover a better workflow, suggest updating playbooks (`~/.codex/prompts/*.md`) or AGENTS notes.
- When blocked, propose concrete research topics to explore (e.g. "<tech> best practices", "<lib> migration patterns").
```

You can customise this to your own conventions; the important part is having a global AGENTS.md so every session starts
with sane defaults.

---

## 5. (Optional) Seed custom prompts (playbooks)

Codex supports slash commands backed by Markdown files in `~/.codex/prompts/`.

1. Create the prompts directory:

   ```bash
   mkdir -p ~/.codex/prompts
   ```

2. Add a starter prompt, e.g. `~/.codex/prompts/draft-pr.md`:

   ```markdown
   ---
   description: Draft a PR description with functional/non-functional changes
   argument-hint: TITLE=<short title> [SCOPE=<modules>]
   ---

   You are drafting a pull request description.

   Inputs:
   - Title: $TITLE
   - Scope: $SCOPE

   Structure the output as:

   1. **Summary**
   2. **Functional changes**
   3. **Non-functional changes** (performance, safety, docs, tests)
   4. **Risk & rollout**
   5. **Testing**
   ```

3. Restart Codex, then run:

   ```text
   /prompts:draft-pr TITLE="Short title" SCOPE="module-name"
   ```

Each `.md` file in `~/.codex/prompts/` becomes `/prompts:<filename>`.

---

## 6. Smoke-test the setup

From a test directory:

```bash
mkdir -p ~/codex-test
cd ~/codex-test
git init
codex
```

Check that:

1. Codex launches a TUI and prompts you inside your repo.
2. It recognises your global `AGENTS.md` (you can ask: "What global instructions are you following?").
3. Slash commands like `/help` work; if you added prompts, `/prompts:draft-pr ...` should show up in the command picker.

If all of that works, the new user is ready to start using Codex on real projects.

---

If you would like, this can also be turned into a `~/.codex/prompts/bootstrap-codex.md` slash-command playbook so an
already-installed Codex can walk someone else through these steps interactively.
