/*
 * Add referential integrity constraint from:
 *
 * - settingsinfo(workspace) -> workspaceindo(id)
 * - serviceinfo(workspace) -> workspaceinfo(id)
 *
 * And unique indexes for:
 *
 * - settingsinfo(workspace)
 * - serviceinfo("@type", workspace)
 */

/*
 * Delete possible duplicate settingsinfo by workspace before enforcing uniqueness.
 */
DELETE FROM settingsinfo WHERE id IN( SELECT id FROM (
  SELECT id,
  ROW_NUMBER() OVER(PARTITION BY workspace ORDER BY id ASC) AS duprow
  FROM settingsinfo
) dups
WHERE
dups.duprow > 1);

/*
 * Delete possible duplicate serviceinfos by type and workspace before enforcing uniqueness.
 */
DELETE FROM serviceinfo WHERE id IN( SELECT id FROM (
  SELECT id,
  ROW_NUMBER() OVER(PARTITION BY "@type", workspace ORDER BY id ASC) AS duprow
  FROM serviceinfo
) dups
WHERE
dups.duprow > 1);

ALTER TABLE settingsinfo
  ADD CONSTRAINT fk_settingsinfo_workspace
  FOREIGN KEY (workspace)
  REFERENCES workspaceinfo(id)
  ON DELETE CASCADE;

CREATE UNIQUE INDEX settingsinfo_workspace_key
  ON settingsinfo (workspace)
  NULLS NOT DISTINCT;

ALTER TABLE serviceinfo
  ADD CONSTRAINT fk_serviceinfo_workspace
  FOREIGN KEY (workspace)
  REFERENCES workspaceinfo(id)
  ON DELETE CASCADE;

CREATE UNIQUE INDEX serviceinfo_workspace_key
  ON serviceinfo ("@type", workspace)
  NULLS NOT DISTINCT;
