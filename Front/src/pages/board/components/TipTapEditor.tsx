import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Link from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import { useCallback } from 'react';

interface TipTapEditorProps {
  content: string;
  onChange: (content: string) => void;
  placeholder?: string;
}

export default function TipTapEditor({ content, onChange, placeholder = '내용을 입력해주세요...' }: TipTapEditorProps) {
  const editor = useEditor({
    extensions: [
      StarterKit,
      Link.configure({ openOnClick: false }),
      Placeholder.configure({ placeholder }),
    ],
    content: content ? JSON.parse(content) : undefined,
    onUpdate: ({ editor: e }) => {
      onChange(JSON.stringify(e.getJSON()));
    },
  });

  const setLink = useCallback(() => {
    if (!editor) return;
    const previousUrl = editor.getAttributes('link').href as string | undefined;
    const url = window.prompt('URL을 입력하세요', previousUrl ?? '');
    if (url === null) return;
    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
      return;
    }
    editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
  }, [editor]);

  if (!editor) return null;

  return (
    <div className="rounded-xl border border-gray-200 focus-within:border-primary-400 focus-within:ring-2 focus-within:ring-primary-50">
      {/* Toolbar */}
      <div className="flex flex-wrap gap-1 border-b border-gray-100 px-3 py-2">
        <ToolbarButton
          active={editor.isActive('bold')}
          onClick={() => editor.chain().focus().toggleBold().run()}
          label="B"
          className="font-bold"
        />
        <ToolbarButton
          active={editor.isActive('italic')}
          onClick={() => editor.chain().focus().toggleItalic().run()}
          label="I"
          className="italic"
        />
        <div className="mx-1 w-px bg-gray-200" />
        <ToolbarButton
          active={editor.isActive('heading', { level: 1 })}
          onClick={() => editor.chain().focus().toggleHeading({ level: 1 }).run()}
          label="H1"
        />
        <ToolbarButton
          active={editor.isActive('heading', { level: 2 })}
          onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}
          label="H2"
        />
        <ToolbarButton
          active={editor.isActive('heading', { level: 3 })}
          onClick={() => editor.chain().focus().toggleHeading({ level: 3 }).run()}
          label="H3"
        />
        <div className="mx-1 w-px bg-gray-200" />
        <ToolbarButton
          active={editor.isActive('bulletList')}
          onClick={() => editor.chain().focus().toggleBulletList().run()}
          label="&bull;"
        />
        <ToolbarButton
          active={editor.isActive('orderedList')}
          onClick={() => editor.chain().focus().toggleOrderedList().run()}
          label="1."
        />
        <div className="mx-1 w-px bg-gray-200" />
        <ToolbarButton
          active={editor.isActive('link')}
          onClick={setLink}
          label="Link"
        />
      </div>

      {/* Editor Content */}
      <EditorContent
        editor={editor}
        className="prose prose-sm max-w-none px-4 py-3 min-h-[200px] focus:outline-none [&_.tiptap]:outline-none [&_.tiptap]:min-h-[200px] [&_.is-editor-empty:first-child::before]:text-gray-400 [&_.is-editor-empty:first-child::before]:float-left [&_.is-editor-empty:first-child::before]:pointer-events-none [&_.is-editor-empty:first-child::before]:h-0"
      />
    </div>
  );
}

interface ToolbarButtonProps {
  active: boolean;
  onClick: () => void;
  label: string;
  className?: string;
}

function ToolbarButton({ active, onClick, label, className = '' }: ToolbarButtonProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`rounded-lg px-2 py-1 text-xs transition-colors ${
        active
          ? 'bg-primary-100 text-primary-600'
          : 'text-gray-500 hover:bg-gray-100 hover:text-gray-700'
      } ${className}`}
    >
      {label}
    </button>
  );
}
