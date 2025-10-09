package com.yandex.app.service;

import com.yandex.app.model.Task;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {

    public Node<Task> head;
    protected Node<Task> tail;
    private final Map<Integer, Node<Task>> idToNode = new HashMap<>();

    @Override
    public void add(Task task) {
        if (!idToNode.isEmpty() && idToNode.containsKey(task.getId())) {
            removeNode(idToNode.get(task.getId()));
        }
        idToNode.put(task.getId(), linkLast(task));
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    @Override
    public void remove(int id) {
        if (idToNode.containsKey(id)) {
            removeNode(idToNode.get(id));
            idToNode.remove(id);
        }
    }

    private Node<Task> linkLast(Task task) {
        final Node<Task> oldTail = tail;
        final Node<Task> newNode = new Node<>(oldTail, task, null);
        tail = newNode;
        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }
        return newNode;
    }

    private List<Task> getTasks() {
        List<Task> listTasks = new ArrayList<>();
        Node<Task> current = head;
        while (current != null) {
            listTasks.add(current.task);
            current = current.next;
        }
        return listTasks;
    }

    private void removeNode(Node<Task> nodeToRemove) {
        if (nodeToRemove == head) {
            head = head.next;
            if (head != null) {
                head.prev = null;
            } else {
                tail = null;
            }
        } else if (nodeToRemove == tail) {
            tail = tail.prev;
            if (tail != null) {
                tail.next = null;
            } else {
                head = null;
            }
        } else {
            nodeToRemove.prev.next = nodeToRemove.next;
            nodeToRemove.next.prev = nodeToRemove.prev;
        }
        nodeToRemove.prev = null;
        nodeToRemove.next = null;
    }
}