import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Link from '@tiptap/extension-link';

interface TipTapViewerProps {
  content: string;
}

export default function TipTapViewer({ content }: TipTapViewerProps) {
  const parsedContent = (() => {
    if (!content) return undefined;
    try {
      return JSON.parse(content);
    } catch {
      return content;
    }
  })();

  const editor = useEditor({
    extensions: [
      StarterKit,
      Link.configure({ openOnClick: true }),
    ],
    content: parsedContent,
    editable: false,
  });

  if (!editor) return null;

  return (
    <EditorContent
      editor={editor}
      className="prose prose-sm max-w-none [&_a]:text-primary-500 [&_a]:underline"
    />
  );
}
